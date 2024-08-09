/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.retrieveEops

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatus
import api.models.domain.{BusinessId, DateRange, Nino}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v2.models.response.domain.{BusinessObligation, ObligationDetail}
import v2.retrieveEops.model.request.RetrieveEOPSObligationsRequest
import v2.retrieveEops.model.response.RetrieveEOPSObligationsResponse

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveEOPSObligationsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveEOPSObligationsService
    with MockRetrieveEOPSObligationsValidatorFactory
    with MockAuditService
    with MockAppConfig {

  private val typeOfBusiness = MtdBusiness.`self-employment`
  private val businessId     = "XAIS123456789012"
  private val fromDate       = "2018-04-06"
  private val toDate         = "2019-04-05"
  private val dueDate        = "2020-04-05"
  private val status         = MtdStatus.Open

  private val requestData = RetrieveEOPSObligationsRequest(
    nino = Nino(nino),
    typeOfBusiness = Some(typeOfBusiness),
    businessId = Some(BusinessId(businessId)),
    dateRange = Some(DateRange(LocalDate.parse(fromDate), LocalDate.parse(toDate))),
    status = Some(status)
  )

  private val responseBodyModel = RetrieveEOPSObligationsResponse(
    obligations = Seq(
      BusinessObligation(
        typeOfBusiness = MtdBusiness.`self-employment`,
        businessId = businessId,
        obligationDetails = Seq(
          ObligationDetail(
            periodStartDate = fromDate,
            periodEndDate = toDate,
            dueDate = dueDate,
            receivedDate = None,
            status = status
          )
        )
      )
    )
  )

  private val responseJson = Json.parse(s"""{
       |  "obligations": [
       |    {
       |      "typeOfBusiness": "$typeOfBusiness",
       |      "businessId": "$businessId",
       |      "obligationDetails": [
       |        {
       |          "periodStartDate": "$fromDate",
       |          "periodEndDate": "$toDate",
       |          "dueDate": "$dueDate",
       |          "status": "$status"
       |        }
       |      ]
       |    }
       |  ]
       |}""".stripMargin)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveEOPSObligationsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBodyModel))))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(responseJson),
          maybeAuditResponseBody = Some(responseJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveEOPSObligationsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: RetrieveEOPSObligationsController = new RetrieveEOPSObligationsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveEOPSObligationsValidatorFactory,
      service = mockService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "retrieveEOPSObligations",
        transactionName = "retrieve-eops-obligations",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          pathParams = Map("nino" -> nino),
          queryParams = None,
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    protected def callController(): Future[Result] =
      controller.handleRequest(
        nino = nino,
        typeOfBusiness = Some(typeOfBusiness.toString),
        businessId = Some(businessId),
        fromDate = Some(fromDate),
        toDate = Some(toDate),
        status = Some(status.toString)
      )(fakeRequest)

  }

}
