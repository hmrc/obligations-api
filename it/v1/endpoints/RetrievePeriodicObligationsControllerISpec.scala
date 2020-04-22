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
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors.{BusinessIdFormatError, FromDateFormatError, MissingTypeOfBusinessError, MtdError, NinoFormatError, StatusFormatError, ToDateFormatError, TypeOfBusinessFormatError}
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrievePeriodicObligationsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val typeOfBusiness = "self-employment"
    val incomeSourceId = "XAIS123456789012"
    val fromDate = "2019-01-01"
    val toDate = "2019-06-06"
    val status = "Open"
    val correlationId = "X-123"

    def setupStubs(): StubMapping

    def uri: String = s"/$nino/income-and-expenditure"

    def desUri: String = s"/enterprise/obligation-data/nino/$nino/ITSA"

    def queryParams: Map[String, String] = Map (
      "from" -> "2019-01-01",
      "to" -> "2019-06-06"
    )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    val responseBody = Json.parse(
      """{
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

    val desResponse: JsValue = Json.parse(
      """
        |{
        |    "obligations": [
        |        {
        |            "identification": {
        |                "incomeSourceType": "ITSB",
        |                "referenceNumber": "XAIS123456789012",
        |                "referenceType": "IncomeSourceId"
        |            },
        |            "obligationDetails": [
        |                {
        |                    "status": "O",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-06-06",
        |                    "inboundCorrespondenceDateReceived": "2019-04-25",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#001"
        |                }
        |            ]
        |        }
        |    ]
        |}
        |""".stripMargin
    )

    def errorBody(code: String): String =
      s"""
         |{
         |     "code": "$code",
         |     "reason": "des message"
         |}
    """.stripMargin

  }

  "Calling the retrieve periodic obligations endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, Status.OK, desResponse)
        }

        val response: WSResponse = await(request().withQueryStringParameters(
          "typeOfBusiness" -> typeOfBusiness,
          "incomeSourceId" -> incomeSourceId,
          "fromDate" -> fromDate,
          "toDate" -> toDate,
          "status" -> status).get())

        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTypeOfBusiness: String,
                                requestIncomeSourceId: String,
                                requestFromDate: String,
                                requestToDate: String,
                                requestStatus: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val typeOfBusiness: String = requestTypeOfBusiness
            override val incomeSourceId: String = requestIncomeSourceId
            override val fromDate: String = requestFromDate
            override val toDate: String = requestToDate
            override val status: String = requestStatus

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().withQueryStringParameters(
              "typeOfBusiness" -> typeOfBusiness,
              "incomeSourceId" -> incomeSourceId,
              "fromDate" -> fromDate,
              "toDate" -> toDate,
              "status" -> status).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }
        val input = Seq(
          ("AA1", "self-employment", "XAIS123456789012", "2019-01-01", "2019-06-06", "Open", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "self-employment", "XAI", "2019-01-01", "2019-06-06", "Open", Status.BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "self-employment", "XAIS123456789012", "2019-01", "2019-06-06", "Open", Status.BAD_REQUEST, FromDateFormatError),
          ("AA123456A", "self-employment", "XAIS123456789012", "2019-01-01", "2019-06", "Open", Status.BAD_REQUEST, ToDateFormatError),
          ("AA123456A", "self-employment", "XAIS123456789012", "2019-01-01", "2019-06-06", "Closed", Status.BAD_REQUEST, StatusFormatError),
          ("AA123456A", "walrus", "XAIS123456789012", "2019-01-01", "2019-06-06", "Open", Status.BAD_REQUEST, TypeOfBusinessFormatError),
          ("AA123456A", "walrus", "XAIS123456789012", "2019-01-01", "2019-06-06", "Open", Status.BAD_REQUEST, TypeOfBusinessFormatError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }
    }


  }
}
