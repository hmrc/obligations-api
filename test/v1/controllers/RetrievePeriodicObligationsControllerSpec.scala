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

import api.controllers.{ ControllerBaseSpec, ControllerTestRunner }
import api.mocks.services.MockAuditService
import api.models.audit.{ AuditEvent, AuditResponse, GenericAuditDetail }
import api.models.domain.Nino
import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatus
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.Result
import v1.mocks.requestParsers.MockRetrievePeriodicObligationsRequestParser
import v1.mocks.services.MockRetrievePeriodicObligationsService
import v1.models.request.retrievePeriodObligations.{ RetrievePeriodicObligationsRawData, RetrievePeriodicObligationsRequest }
import v1.models.response.common.{ Obligation, ObligationDetail }
import v1.models.response.retrievePeriodicObligations.RetrievePeriodObligationsResponse

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePeriodicObligationsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrievePeriodicObligationsService
    with MockRetrievePeriodicObligationsRequestParser
    with MockAuditService {

  private val typeOfBusiness = "self-employment"
  private val businessId     = "XAIS123456789012"
  private val fromDate       = "2019-01-01"
  private val toDate         = "2019-06-06"
  private val status         = "Open"

  private val rawData = RetrievePeriodicObligationsRawData(
    nino,
    Some(typeOfBusiness),
    Some(businessId),
    Some(fromDate),
    Some(toDate),
    Some(status)
  )

  private val requestData =
    RetrievePeriodicObligationsRequest(
      Nino(nino),
      Some(MtdBusiness.`self-employment`),
      Some(businessId),
      Some(fromDate),
      Some(toDate),
      Some(MtdStatus.Open)
    )

  private val response = RetrievePeriodObligationsResponse(
    Seq(
      Obligation(MtdBusiness.`self-employment`,
                 businessId,
                 Seq(
                   ObligationDetail(
                     fromDate,
                     toDate,
                     "2019-04-30",
                     Some("2019-04-25"),
                     MtdStatus.Open
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
                                          |           "status": "Open"
                                          |         }
                                          |       ]
                                          |    }
                                          |  ]
                                          |}
                                          |""".stripMargin)

  "handleRequest" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {

        MockRetrievePeriodicObligationsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

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
      "parser errors occur" must {}

      "service errors occur" must {}
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    val controller = new RetrievePeriodicObligationsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestParser,
      service = mockService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
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
  }
}
