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

package v3.retrievePeriodic

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatusV3
import api.models.domain.{BusinessId, DateRange, Nino}
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v3.models.response.domain.{BusinessObligation, ObligationDetail}
import v3.retrievePeriodic.model.request.RetrievePeriodicObligationsRequest
import v3.retrievePeriodic.model.response.RetrievePeriodObligationsResponse

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePeriodicObligationsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrievePeriodicObligationsService
    with MockRetrievePeriodicObligationsValidatorFactory
    with MockAuditService
    with MockAppConfig {

  private val typeOfBusiness = "self-employment"
  private val businessId     = "XAIS123456789012"
  private val fromDate       = "2019-01-01"
  private val toDate         = "2019-06-06"
  private val status         = "Open"

  private val requestData =
    RetrievePeriodicObligationsRequest(
      Nino(nino),
      Some(MtdBusiness.`self-employment`),
      Some(BusinessId(businessId)),
      dateRange = Some(DateRange(LocalDate.parse(fromDate), LocalDate.parse(toDate))),
      Some(MtdStatusV3.open)
    )

  private val response = RetrievePeriodObligationsResponse(
    Seq(
      BusinessObligation(
        MtdBusiness.`self-employment`,
        businessId,
        Seq(
          ObligationDetail(
            fromDate,
            toDate,
            "2019-04-30",
            Some("2019-04-25"),
            MtdStatusV3.open
          )
        ))
    )
  )

  private val responseJson = Json.parse("""{
                                          |  "obligations": [
                                          |     {
                                          |       "typeOfBusiness": "self-employment",
                                          |       "businessId": "XAIS123456789012",
                                          |       "obligationDetails": [
                                          |         {
                                          |           "periodStartDate": "2019-01-01",
                                          |           "periodEndDate": "2019-06-06",
                                          |           "dueDate": "2019-04-30",
                                          |           "receivedDate": "2019-04-25",
                                          |           "status": "open"
                                          |         }
                                          |       ]
                                          |    }
                                          |  ]
                                          |}
                                          |""".stripMargin)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrievePeriodicObligationsService
          .retrieve(requestData)
          .returns(
            Future.successful(Right(ResponseWrapper(correlationId, response)))
          )

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = None,
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

        MockRetrievePeriodicObligationsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: RetrievePeriodicObligationsController = new RetrievePeriodicObligationsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrievePeriodicObligationsValidatorFactory,
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
        auditType = "retrievePeriodicObligations",
        transactionName = "retrieve-periodic-obligations",
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
        nino,
        Some(typeOfBusiness),
        Some(businessId),
        Some(fromDate),
        Some(toDate),
        Some(status)
      )(fakeRequest)

  }

}
