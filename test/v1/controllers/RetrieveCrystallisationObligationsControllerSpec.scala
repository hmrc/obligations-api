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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.Nino
import api.models.domain.status.MtdStatus
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.controllers.validators.MockRetrieveCrystallisationObligationsValidatorFactory
import v1.mocks.services._
import v1.models.request.ObligationsTaxYear
import v1.models.request.retrieveCrystallisationObligations._
import v1.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCrystallisationObligationsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveCrystallisationObligationsService
    with MockRetrieveCrystallisationObligationsValidatorFactory
    with MockAuditService
    with MockAppConfig {

  private val taxYear = "2017-18"

  private val requestData = RetrieveCrystallisationObligationsRequest(Nino(nino), ObligationsTaxYear("2017-04-06", "2018-04-05"))

  private val responseBodyModel: RetrieveCrystallisationObligationsResponse =
    RetrieveCrystallisationObligationsResponse("2018-04-06", "2019-04-05", "2020-01-31", MtdStatus.Fulfilled, Some("2020-01-25"))

  private val responseJson: JsValue = Json.parse("""{
    |  "periodStartDate": "2018-04-06",
    |  "periodEndDate": "2019-04-05",
    |  "dueDate": "2020-01-31",
    |  "status": "Fulfilled",
    |  "receivedDate": "2020-01-25"
    |}
    """.stripMargin)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveCrystallisationObligationsService
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

        MockRetrieveCrystallisationObligationsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: RetrieveCrystallisationObligationsController = new RetrieveCrystallisationObligationsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveCrystallisationObligationsValidatorFactory,
      service = mockRetrieveCrystallisationObligationsService,
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
        auditType = "RetrieveCrystallisationObligations",
        transactionName = "retrieve-crystallisation-obligations",
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

    protected def callController(): Future[Result] = controller.handleRequest(nino, Some(taxYear))(fakeGetRequest)

  }

}
