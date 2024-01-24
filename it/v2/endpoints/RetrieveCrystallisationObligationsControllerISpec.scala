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

package v2.endpoints

import api.models.errors._
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.{ WSRequest, WSResponse }
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.stubs.{ AuditStub, AuthStub, DesStub, MtdIdLookupStub }

class RetrieveCrystallisationObligationsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino                                     = "AA123456A"
    val taxYear                                  = "2017-18"
    val singleObligationResponseBody: JsValue    = Json.parse(s"""
         |{
         |  "obligations": [
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
    val multipleObligationsResponseBody: JsValue = Json.parse(s"""
                                                                 |{
                                                                 |  "obligations": [
                                                                 |    {
                                                                 |      "periodStartDate": "2018-04-06",
                                                                 |      "periodEndDate": "2019-04-05",
                                                                 |      "dueDate": "2020-01-31",
                                                                 |      "status": "Fulfilled",
                                                                 |      "receivedDate": "2020-01-25"
                                                                 |    },
                                                                 |    {
                                                                 |      "periodStartDate": "2018-04-06",
                                                                 |      "periodEndDate": "2019-04-05",
                                                                 |      "dueDate": "2020-01-31",
                                                                 |      "status": "Fulfilled",
                                                                 |      "receivedDate": "2021-01-25"
                                                                 |    }
                                                                 |  ]
                                                                 |}
                                                                 |""".stripMargin)
    val desResponse: JsValue                     = Json.parse("""
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
        |                    "periodKey": "#001"
        |                }
        |            ]
        |        }
        |    ]
        |}
    """.stripMargin)

    def desUri: String = s"/enterprise/obligation-data/nino/$nino/ITSA"

    def queryParams: Map[String, String] = Map(
      "from" -> "2017-04-06",
      "to"   -> "2018-04-05"
    )

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()

      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def setupStubs(): Unit = {}

    def uri: String = s"/$nino/crystallisation"

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

        override def setupStubs(): Unit = {
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
        }

        val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
        response.status shouldBe OK
        response.json shouldBe singleObligationResponseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "DES returns more than one obligation but only one has incomeSourceId ITSA" in new Test {
        override val desResponse: JsValue = Json.parse("""
            | {
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSF",
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
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        },
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
            |                    "periodKey": "#002"
            |                }
            |            ]
            |        }
            |    ]
            |}
    """.stripMargin)

        override def setupStubs(): Unit = {
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
        }

        val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
        response.status shouldBe OK
        response.json shouldBe singleObligationResponseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "more than one obligation is returned in the obligations array" in new Test {
        override val desResponse: JsValue = Json.parse("""
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
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSA",
            |                "referenceNumber": "AB123456B",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2021-01-25",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#002"
            |                }
            |            ]
            |        }
            |    ]
            |}""".stripMargin)

        override def setupStubs(): Unit = {
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
        }

        val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
        response.status shouldBe OK
        response.json shouldBe multipleObligationsResponseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "more than one obligation is returned in the obligationDetails array" in new Test {
        override val desResponse: JsValue = Json.parse("""
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
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2021-01-25",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#002"
            |                }
            |            ]
            |        }
            |    ]
            |}""".stripMargin)

        override def setupStubs(): Unit = {
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
        }

        val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
        response.status shouldBe OK
        response.json shouldBe multipleObligationsResponseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA1123A", "2017-18", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2016-17", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2017-19", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): Unit = {
              DesStub.onError(DesStub.GET, desUri, queryParams, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_STATUS", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_REGIME", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_DATE_FROM", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_DATE_TO", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_DATE_RANGE", INTERNAL_SERVER_ERROR, InternalError),
          (FORBIDDEN, "NOT_FOUND_BPKEY", NOT_FOUND, NotFoundError),
          (FORBIDDEN, "INSOLVENT_TRADER", BAD_REQUEST, RuleInsolventTraderError),
          (BAD_REQUEST, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))

        "no obligations found" when {
          "the backend returns a 200 but nothing returned is ITSA" in new Test {

            override val desResponse: JsValue = Json.parse("""
                | {
                |    "obligations": [
                |        {
                |            "identification": {
                |                "incomeSourceType": "ITSF",
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
                |                    "periodKey": "#001"
                |                }
                |            ]
                |        }
                |    ]
                |}
    """.stripMargin)

            override def setupStubs(): Unit = {
              DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
            }

            val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).get())
            response.status shouldBe NOT_FOUND
            response.json shouldBe Json.toJson(NoObligationsFoundError)
          }
        }
      }
    }
  }
}
