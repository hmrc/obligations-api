/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveCrystallisationObligationsRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveCrystallisationObligationsService}
import v1.models.domain.status.MtdStatus
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.ObligationsTaxYear
import v1.models.request.retrieveCrystallisationObligations._
import v1.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCrystallisationObligationsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveCrystallisationObligationsService
    with MockRetrieveCrystallisationObligationsRequestParser
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new RetrieveCrystallisationObligationsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino          = "AA123456A"
  private val taxYear       = "2017-18"
  private val correlationId = "X-123"

  private val responseBody = Json.parse("""{
                                          |  "periodStartDate": "2018-04-06",
                                          |  "periodEndDate": "2019-04-05",
                                          |  "dueDate": "2020-01-31",
                                          |  "status": "Fulfilled",
                                          |  "receivedDate": "2020-01-25"
                                          |}
    """.stripMargin)

  private val rawData     = RetrieveCrystallisationObligationsRawData(nino, Some(taxYear))
  private val requestData = RetrieveCrystallisationObligationsRequest(Nino(nino), ObligationsTaxYear("2017-04-06", "2018-04-05"))

  "handleRequest" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveCrystallisationObligationsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveCrystallisationObligationsService
          .retrieve(requestData)
          .returns(Future.successful(
            Right(ResponseWrapper(correlationId, RetrieveCrystallisationObligationsResponse(
              "2018-04-06", "2019-04-05", "2020-01-31", MtdStatus.Fulfilled, Some("2020-01-25")
            )))
          ))

        val result: Future[Result] = controller.handleRequest(nino, Some(taxYear))(fakeRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseBody
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveCrystallisationObligationsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(nino, Some(taxYear))(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeExceededError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveCrystallisationObligationsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveCrystallisationObligationsService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, Some(taxYear))(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (NoObligationsFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
