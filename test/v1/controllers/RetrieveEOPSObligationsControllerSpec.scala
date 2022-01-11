/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.controllers

import play.api.libs.json.Json
import play.api.mvc.Result
import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveEOPSObligationsRequestParser
import v1.mocks.services.{ MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveEOPSObligationsService }
import v1.models.audit.{ AuditError, AuditEvent, AuditResponse, RetrieveEOPSObligationsAuditDetail }
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.MtdStatus
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveEOPSObligations._
import v1.models.response.common.{ Obligation, ObligationDetail }
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveEOPSObligationsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveEOPSObligationsService
    with MockRetrieveEOPSObligationsRequestParser
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new RetrieveEOPSObligationsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestParser,
      service = mockService,
      auditService = mockAuditService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino           = "AA123456A"
  private val typeOfBusiness = MtdBusiness.`self-employment`
  private val businessId     = "XAIS123456789012"
  private val fromDate       = "2018-04-06"
  private val toDate         = "2019-04-05"
  private val status         = MtdStatus.Open
  private val correlationId  = "X-123"

  private val responseBody = Json.parse(s"""{
      |  "obligations": [
      |    {
      |      "typeOfBusiness": "$typeOfBusiness",
      |      "businessId": "$businessId",
      |      "obligationDetails": [
      |        {
      |          "periodStartDate": "$fromDate",
      |          "periodEndDate": "$toDate",
      |          "dueDate": "2020-04-05",
      |          "status": "$status"
      |        }
      |      ]
      |    }
      |  ]
      |}""".stripMargin)

  def event(auditResponse: AuditResponse): AuditEvent[RetrieveEOPSObligationsAuditDetail] =
    AuditEvent(
      auditType = "retrieveEOPSObligations",
      transactionName = "retrieve-eops-obligations",
      detail = RetrieveEOPSObligationsAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        Some("self-employment"),
        Some(businessId),
        Some(fromDate),
        Some(toDate),
        Some("Open"),
        correlationId,
        auditResponse
      )
    )

  private val rawData = RetrieveEOPSObligationsRawData(
    nino = nino,
    typeOfBusiness = Some(typeOfBusiness.toString),
    businessId = Some(businessId),
    fromDate = Some(fromDate),
    toDate = Some(toDate),
    status = Some(status.toString)
  )
  private val requestData = RetrieveEOPSObligationsRequest(
    nino = Nino(nino),
    typeOfBusiness = Some(typeOfBusiness),
    businessId = Some(businessId),
    fromDate = Some(fromDate),
    toDate = Some(toDate),
    status = Some(status)
  )

  "handleRequest" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveEOPSObligationsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveEOPSObligationsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(
            correlationId,
            RetrieveEOPSObligationsResponse(
              obligations = Seq(
                Obligation(
                  typeOfBusiness = MtdBusiness.`self-employment`,
                  businessId = businessId,
                  obligationDetails = Seq(
                    ObligationDetail(
                      periodStartDate = fromDate,
                      periodEndDate = toDate,
                      dueDate = "2020-04-05",
                      receivedDate = None,
                      status = status
                    )
                  )
                )
              )
            )
          ))))

        val result: Future[Result] = controller.handleRequest(
          nino = nino,
          typeOfBusiness = Some(typeOfBusiness.toString),
          businessId = Some(businessId),
          fromDate = Some(fromDate),
          toDate = Some(toDate),
          status = Some(status.toString)
        )(fakeRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseBody
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveEOPSObligationsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(
              nino = nino,
              typeOfBusiness = Some(typeOfBusiness.toString),
              businessId = Some(businessId),
              fromDate = Some(fromDate),
              toDate = Some(toDate),
              status = Some(status.toString)
            )(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TypeOfBusinessFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (StatusFormatError, BAD_REQUEST),
          (MissingFromDateError, BAD_REQUEST),
          (MissingToDateError, BAD_REQUEST),
          (ToDateBeforeFromDateError, BAD_REQUEST),
          (MissingTypeOfBusinessError, BAD_REQUEST),
          (RuleDateRangeInvalidError, BAD_REQUEST),
          (RuleFromDateNotSupportedError, BAD_REQUEST),
          (BadRequestError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a ${mtdError.code} error is returned from the service" in new Test {

            MockRetrieveEOPSObligationsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveEOPSObligationsService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(
              nino = nino,
              typeOfBusiness = Some(typeOfBusiness.toString),
              businessId = Some(businessId),
              fromDate = Some(fromDate),
              toDate = Some(toDate),
              status = Some(status.toString)
            )(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (RuleDateRangeInvalidError, BAD_REQUEST),
          (RuleInsolventTraderError, FORBIDDEN),
          (NotFoundError, NOT_FOUND),
          (NoObligationsFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
