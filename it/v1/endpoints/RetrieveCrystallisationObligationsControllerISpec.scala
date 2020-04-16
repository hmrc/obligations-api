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
import v1.models.errors._
import v1.models.request.DesTaxYear
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrieveCrystallisationObligationsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val taxYear = "2017-18"
    val correlationId = "X-123"

    def setupStubs(): StubMapping

    def uri: String = s"/$nino/crystallisation"

    def desUri: String = s"/enterprise/obligation-data/nino/$nino/ITSA"

    def queryParams: Map[String, String] = Map (
      "from" -> "2017-04-06",
      "to" -> "2018-04-05"
    )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    val responseBody: JsValue = Json.parse(s"""
                                              |{
                                              |  "obligationDetails": [
                                              |    {
                                              |      "periodStartDate": "2018-04-06",
                                              |      "periodEndDate": "2019-04-05",
                                              |      "dueDate": "2020-01-31",
                                              |      "status": "Fulfilled",
                                              |      "receivedDate": "2020-01-25"
                                              |    }
                                              |  ]
                                              |}
                                              |""".stripMargin)

    val desResponse: JsValue = Json.parse(
      """
        | {
        |    "obligations": [
        |        {
        |            "identification": {
        |                "incomeSourceType": "ITSA",
        |                "referenceNumber": "AB123456A",
        |                "referenceType": "NINO"
        |            },
        |            "obligationDetails": [
        |                {
        |                    "status": "F",
        |                    "inboundCorrespondenceFromDate": "2018-04-06",
        |                    "inboundCorrespondenceToDate": "2019-04-05",
        |                    "inboundCorrespondenceDateReceived": "2020-01-25",
        |                    "inboundCorrespondenceDueDate": "2020-01-31",
        |                    "periodKey": "ITSA"
        |                }
        |            ]
        |        }
        |    ]
        |}
    """.stripMargin)

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "Calling the retrieve crystallisation obligations endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, Status.OK, desResponse)
        }

        val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2017-18", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2016-17", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2017-19", Status.BAD_REQUEST, RuleTaxYearRangeExceededError)
        )


        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.GET, desUri, queryParams, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_IDNUMBER", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_IDTYPE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_STATUS", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_REGIME", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_DATE_FROM", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_DATE_TO", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_DATE_RANGE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.FORBIDDEN, "NOT_FOUND_BPKEY", Status.NOT_FOUND, NotFoundError),
          (Status.BAD_REQUEST, "NOT_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
