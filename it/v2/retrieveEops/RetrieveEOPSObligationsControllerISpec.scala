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

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v2.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrieveEOPSObligationsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino                  = "AA123456A"
    val typeOfBusiness        = "self-employment"
    val businessId            = "XAIS12345678901"
    val fromDate              = "2018-04-06"
    val toDate                = "2019-04-05"
    val status                = "Open"
    val desResponse: JsValue  = Json.parse("""
        |{
        |    "obligations": [
        |        {
        |            "identification": {
        |                "incomeSourceType": "ITSB",
        |                "referenceNumber": "XAIS12345678901",
        |                "referenceType": "MTDBIS"
        |            },
        |            "obligationDetails": [
        |                {
        |                    "status": "F",
        |                    "inboundCorrespondenceFromDate": "2018-01-01",
        |                    "inboundCorrespondenceToDate": "2018-12-31",
        |                    "inboundCorrespondenceDateReceived": "2019-05-13",
        |                    "inboundCorrespondenceDueDate": "2020-01-31",
        |                    "periodKey": "EOPS"
        |                },
        |                {
        |                    "status": "F",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-03-31",
        |                    "inboundCorrespondenceDateReceived": "2019-04-25",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#001"
        |                },
        |                {
        |                    "status": "F",
        |                    "inboundCorrespondenceFromDate": "2019-04-01",
        |                    "inboundCorrespondenceToDate": "2019-06-30",
        |                    "inboundCorrespondenceDateReceived": "2019-07-01",
        |                    "inboundCorrespondenceDueDate": "2019-07-31",
        |                    "periodKey": "#002"
        |                },
        |                {
        |                    "status": "F",
        |                    "inboundCorrespondenceFromDate": "2019-07-01",
        |                    "inboundCorrespondenceToDate": "2019-09-30",
        |                    "inboundCorrespondenceDateReceived": "2019-10-08",
        |                    "inboundCorrespondenceDueDate": "2019-10-31",
        |                    "periodKey": "#003"
        |                },
        |                {
        |                    "status": "O",
        |                    "inboundCorrespondenceFromDate": "2019-10-01",
        |                    "inboundCorrespondenceToDate": "2019-12-31",
        |                    "inboundCorrespondenceDueDate": "2020-01-31",
        |                    "periodKey": "#004"
        |                },
        |                {
        |                    "status": "O",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-12-31",
        |                    "inboundCorrespondenceDueDate": "2021-01-31",
        |                    "periodKey": "EOPS"
        |                }
        |            ]
        |        }
        |    ]
        |}""".stripMargin)
    val responseBody: JsValue = Json.parse(s"""
                                              |{
                                              |  "obligations": [
                                              |     {
                                              |       "typeOfBusiness": "self-employment",
                                              |       "businessId": "XAIS12345678901",
                                              |       "obligationDetails": [
                                              |         {
                                              |           "periodStartDate": "2018-01-01",
                                              |           "periodEndDate": "2018-12-31",
                                              |           "dueDate": "2020-01-31",
                                              |           "receivedDate": "2019-05-13",
                                              |           "status": "Fulfilled"
                                              |         },
                                              |         {
                                              |           "periodStartDate": "2019-01-01",
                                              |           "periodEndDate": "2019-12-31",
                                              |           "dueDate": "2021-01-31",
                                              |           "status": "Open"
                                              |         }
                                              |       ]
                                              |    }
                                              |  ]
                                              |}""".stripMargin)

    def setupStubs(): StubMapping

    def desUri: String = s"/enterprise/obligation-data/nino/$nino/ITSA"

    def queryParams: Map[String, String] = Map(
      "from"   -> fromDate,
      "to"     -> toDate,
      "status" -> "O"
    )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def uri: String = s"/$nino/end-of-period-statement"

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "Calling the retrieve EOPS obligations endpoint" should {

    "return a 200 status code" when {

      "a request is made with no optional query parameters" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, OK, desResponse)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a request is made with all query parameters present" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
        }

        val response: WSResponse = await(
          request()
            .withQueryStringParameters(
              "typeOfBusiness" -> typeOfBusiness,
              "businessId"     -> businessId,
              "fromDate"       -> fromDate,
              "toDate"         -> toDate,
              "status"         -> status
            )
            .get())
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a request is made with all query parameters present and DES returns an incomeSourceType to be filtered out" in new Test {

        override val desResponse: JsValue = Json.parse("""
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678901",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-01-01",
            |                    "inboundCorrespondenceToDate": "2018-12-31",
            |                    "inboundCorrespondenceDateReceived": "2019-05-13",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-03-31",
            |                    "inboundCorrespondenceDateReceived": "2019-04-25",
            |                    "inboundCorrespondenceDueDate": "2019-04-30",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-04-01",
            |                    "inboundCorrespondenceToDate": "2019-06-30",
            |                    "inboundCorrespondenceDateReceived": "2019-07-01",
            |                    "inboundCorrespondenceDueDate": "2019-07-31",
            |                    "periodKey": "#002"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-07-01",
            |                    "inboundCorrespondenceToDate": "2019-09-30",
            |                    "inboundCorrespondenceDateReceived": "2019-10-08",
            |                    "inboundCorrespondenceDueDate": "2019-10-31",
            |                    "periodKey": "#003"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-10-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#004"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2021-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678901",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-01-01",
            |                    "inboundCorrespondenceToDate": "2018-12-31",
            |                    "inboundCorrespondenceDateReceived": "2019-05-13",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-03-31",
            |                    "inboundCorrespondenceDateReceived": "2019-04-25",
            |                    "inboundCorrespondenceDueDate": "2019-04-30",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-04-01",
            |                    "inboundCorrespondenceToDate": "2019-06-30",
            |                    "inboundCorrespondenceDateReceived": "2019-07-01",
            |                    "inboundCorrespondenceDueDate": "2019-07-31",
            |                    "periodKey": "#002"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-07-01",
            |                    "inboundCorrespondenceToDate": "2019-09-30",
            |                    "inboundCorrespondenceDateReceived": "2019-10-08",
            |                    "inboundCorrespondenceDueDate": "2019-10-31",
            |                    "periodKey": "#003"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-10-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#004"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2021-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
        }

        val response: WSResponse = await(
          request()
            .withQueryStringParameters(
              "typeOfBusiness" -> typeOfBusiness,
              "businessId"     -> businessId,
              "fromDate"       -> fromDate,
              "toDate"         -> toDate,
              "status"         -> status
            )
            .get())
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a request is made with all query parameters present and DES returns a referenceNumber to be filtered out" in new Test {

        override val desResponse: JsValue = Json.parse("""
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678901",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-01-01",
            |                    "inboundCorrespondenceToDate": "2018-12-31",
            |                    "inboundCorrespondenceDateReceived": "2019-05-13",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-03-31",
            |                    "inboundCorrespondenceDateReceived": "2019-04-25",
            |                    "inboundCorrespondenceDueDate": "2019-04-30",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-04-01",
            |                    "inboundCorrespondenceToDate": "2019-06-30",
            |                    "inboundCorrespondenceDateReceived": "2019-07-01",
            |                    "inboundCorrespondenceDueDate": "2019-07-31",
            |                    "periodKey": "#002"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-07-01",
            |                    "inboundCorrespondenceToDate": "2019-09-30",
            |                    "inboundCorrespondenceDateReceived": "2019-10-08",
            |                    "inboundCorrespondenceDueDate": "2019-10-31",
            |                    "periodKey": "#003"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-10-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#004"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2021-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "OTHER REFERENCE NUMBER",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-01-01",
            |                    "inboundCorrespondenceToDate": "2018-12-31",
            |                    "inboundCorrespondenceDateReceived": "2019-05-13",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-03-31",
            |                    "inboundCorrespondenceDateReceived": "2019-04-25",
            |                    "inboundCorrespondenceDueDate": "2019-04-30",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-04-01",
            |                    "inboundCorrespondenceToDate": "2019-06-30",
            |                    "inboundCorrespondenceDateReceived": "2019-07-01",
            |                    "inboundCorrespondenceDueDate": "2019-07-31",
            |                    "periodKey": "#002"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-07-01",
            |                    "inboundCorrespondenceToDate": "2019-09-30",
            |                    "inboundCorrespondenceDateReceived": "2019-10-08",
            |                    "inboundCorrespondenceDueDate": "2019-10-31",
            |                    "periodKey": "#003"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-10-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#004"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2021-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
        }

        val response: WSResponse = await(
          request()
            .withQueryStringParameters(
              "typeOfBusiness" -> typeOfBusiness,
              "businessId"     -> businessId,
              "fromDate"       -> fromDate,
              "toDate"         -> toDate,
              "status"         -> status
            )
            .get())
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTypeOfBusiness: Option[String],
                                requestBusinessId: Option[String],
                                requestFromDate: Option[String],
                                requestToDate: Option[String],
                                requestStatus: Option[String],
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            override val nino: String = requestNino

            val response: WSResponse = await(
              request()
                .withQueryStringParameters(
                  requestTypeOfBusiness.map("typeOfBusiness" -> _).getOrElse(("", "")),
                  requestBusinessId.map("businessId"         -> _).getOrElse(("", "")),
                  requestFromDate.map("fromDate"             -> _).getOrElse(("", "")),
                  requestToDate.map("toDate"                 -> _).getOrElse(("", "")),
                  requestStatus.map("status"                 -> _).getOrElse(("", ""))
                )
                .get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("BEANS",
           Some("self-employment"),
           Some("XAIS12345678901"),
           Some("2018-04-06"),
           Some("2019-04-05"),
           Some("Open"),
           BAD_REQUEST,
           NinoFormatError),
          ("AA123456A",
            Some("do-not-use"),
            Some("XAIS12345678901"),
            Some("2018-04-06"),
            Some("2019-04-05"),
            Some("Open"),
            BAD_REQUEST,
            TypeOfBusinessFormatError),
          ("AA123456A",
           Some("self-employment"),
           Some("beans"),
           Some("2018-04-06"),
           Some("2019-04-05"),
           Some("Open"),
           BAD_REQUEST,
           BusinessIdFormatError),
          ("AA123456A",
           Some("self-employment"),
           Some("XAIS12345678901"),
           Some("bad-date"),
           Some("2019-04-05"),
           Some("Open"),
           BAD_REQUEST,
           FromDateFormatError),
          ("AA123456A",
           Some("self-employment"),
           Some("XAIS12345678901"),
           Some("2019-04-05"),
           Some("bad-date"),
           Some("Open"),
           BAD_REQUEST,
           ToDateFormatError),
          ("AA123456A",
           Some("self-employment"),
           Some("XAIS12345678901"),
           Some("2018-04-06"),
           Some("2019-04-05"),
           Some("Somewhat-Open"),
           BAD_REQUEST,
           StatusFormatError),
          ("AA123456A", Some("self-employment"), Some("XAIS12345678901"), Some("2019-04-05"), None, Some("Open"), BAD_REQUEST, MissingToDateError),
          ("AA123456A", Some("self-employment"), Some("XAIS12345678901"), None, Some("2019-04-05"), Some("Open"), BAD_REQUEST, MissingFromDateError),
          ("AA123456A",
           Some("self-employment"),
           Some("XAIS12345678901"),
           Some("2020-04-05"),
           Some("2019-04-05"),
           Some("Open"),
           BAD_REQUEST,
           ToDateBeforeFromDateError),
          ("AA123456A", None, Some("XAIS12345678901"), Some("2020-04-05"), Some("2019-04-05"), Some("Open"), BAD_REQUEST, MissingTypeOfBusinessError),
          ("AA123456A",
           Some("self-employment"),
           Some("XAIS12345678901"),
           Some("2019-04-05"),
           Some("2021-04-05"),
           Some("Open"),
           BAD_REQUEST,
           RuleDateRangeInvalidError),
          ("AA123456A",
           Some("self-employment"),
           Some("XAIS12345678901"),
           Some("2016-04-05"),
           Some("2017-04-05"),
           Some("Open"),
           BAD_REQUEST,
           RuleFromDateNotSupportedError)
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

            val response: WSResponse = await(
              request()
                .withQueryStringParameters(
                  "typeOfBusiness" -> typeOfBusiness,
                  "businessId"     -> businessId,
                  "fromDate"       -> fromDate,
                  "toDate"         -> toDate,
                  "status"         -> status
                )
                .get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_STATUS", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_REGIME", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, FromDateFormatError),
          (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, ToDateFormatError),
          (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, RuleDateRangeInvalidError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (FORBIDDEN, "NOT_FOUND_BPKEY", NOT_FOUND, NotFoundError),
          (FORBIDDEN, "INSOLVENT_TRADER", BAD_REQUEST, RuleInsolventTraderError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }

      "DES returns data that is filtered out" when {
        "no MTDBIS obligations are returned" in new Test {
          override val desResponse: JsValue = Json.parse("""
              |{
              |    "obligations": [
              |        {
              |            "identification": {
              |                "incomeSourceType": "ITSB",
              |                "referenceNumber": "XAIS12345678901",
              |                "referenceType": "OTHER"
              |            },
              |            "obligationDetails": [
              |                {
              |                    "status": "F",
              |                    "inboundCorrespondenceFromDate": "2018-01-01",
              |                    "inboundCorrespondenceToDate": "2018-12-31",
              |                    "inboundCorrespondenceDateReceived": "2019-05-13",
              |                    "inboundCorrespondenceDueDate": "2020-01-31",
              |                    "periodKey": "EOPS"
              |                },
              |                {
              |                    "status": "F",
              |                    "inboundCorrespondenceFromDate": "2019-01-01",
              |                    "inboundCorrespondenceToDate": "2019-03-31",
              |                    "inboundCorrespondenceDateReceived": "2019-04-25",
              |                    "inboundCorrespondenceDueDate": "2019-04-30",
              |                    "periodKey": "#001"
              |                },
              |                {
              |                    "status": "F",
              |                    "inboundCorrespondenceFromDate": "2019-04-01",
              |                    "inboundCorrespondenceToDate": "2019-06-30",
              |                    "inboundCorrespondenceDateReceived": "2019-07-01",
              |                    "inboundCorrespondenceDueDate": "2019-07-31",
              |                    "periodKey": "#002"
              |                },
              |                {
              |                    "status": "F",
              |                    "inboundCorrespondenceFromDate": "2019-07-01",
              |                    "inboundCorrespondenceToDate": "2019-09-30",
              |                    "inboundCorrespondenceDateReceived": "2019-10-08",
              |                    "inboundCorrespondenceDueDate": "2019-10-31",
              |                    "periodKey": "#003"
              |                },
              |                {
              |                    "status": "O",
              |                    "inboundCorrespondenceFromDate": "2019-10-01",
              |                    "inboundCorrespondenceToDate": "2019-12-31",
              |                    "inboundCorrespondenceDueDate": "2020-01-31",
              |                    "periodKey": "#004"
              |                },
              |                {
              |                    "status": "O",
              |                    "inboundCorrespondenceFromDate": "2019-01-01",
              |                    "inboundCorrespondenceToDate": "2019-12-31",
              |                    "inboundCorrespondenceDueDate": "2021-01-31",
              |                    "periodKey": "EOPS"
              |                }
              |            ]
              |        }
              |    ]
              |}""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.GET, desUri, OK, desResponse)
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe NOT_FOUND
          response.json shouldBe Json.toJson(NoObligationsFoundError)
        }

        "the incomeSourceType filter results in everything being filtered out" in new Test {
          override val desResponse: JsValue = Json.parse("""
              |{
              |    "obligations": [
              |        {
              |            "identification": {
              |                "incomeSourceType": "ITSB",
              |                "referenceNumber": "XAIS12345678901",
              |                "referenceType": "MTDBIS"
              |            },
              |            "obligationDetails": [
              |                {
              |                    "status": "F",
              |                    "inboundCorrespondenceFromDate": "2018-01-01",
              |                    "inboundCorrespondenceToDate": "2018-12-31",
              |                    "inboundCorrespondenceDateReceived": "2019-05-13",
              |                    "inboundCorrespondenceDueDate": "2020-01-31",
              |                    "periodKey": "EOPS"
              |                },
              |                {
              |                    "status": "F",
              |                    "inboundCorrespondenceFromDate": "2019-01-01",
              |                    "inboundCorrespondenceToDate": "2019-03-31",
              |                    "inboundCorrespondenceDateReceived": "2019-04-25",
              |                    "inboundCorrespondenceDueDate": "2019-04-30",
              |                    "periodKey": "#001"
              |                },
              |                {
              |                    "status": "F",
              |                    "inboundCorrespondenceFromDate": "2019-04-01",
              |                    "inboundCorrespondenceToDate": "2019-06-30",
              |                    "inboundCorrespondenceDateReceived": "2019-07-01",
              |                    "inboundCorrespondenceDueDate": "2019-07-31",
              |                    "periodKey": "#002"
              |                },
              |                {
              |                    "status": "F",
              |                    "inboundCorrespondenceFromDate": "2019-07-01",
              |                    "inboundCorrespondenceToDate": "2019-09-30",
              |                    "inboundCorrespondenceDateReceived": "2019-10-08",
              |                    "inboundCorrespondenceDueDate": "2019-10-31",
              |                    "periodKey": "#003"
              |                },
              |                {
              |                    "status": "O",
              |                    "inboundCorrespondenceFromDate": "2019-10-01",
              |                    "inboundCorrespondenceToDate": "2019-12-31",
              |                    "inboundCorrespondenceDueDate": "2020-01-31",
              |                    "periodKey": "#004"
              |                },
              |                {
              |                    "status": "O",
              |                    "inboundCorrespondenceFromDate": "2019-01-01",
              |                    "inboundCorrespondenceToDate": "2019-12-31",
              |                    "inboundCorrespondenceDueDate": "2021-01-31",
              |                    "periodKey": "EOPS"
              |                }
              |            ]
              |        }
              |    ]
              |}""".stripMargin)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.GET, desUri, OK, desResponse)
          }

          override val businessId = "XAIS12345678903"

          val response: WSResponse = await(
            request()
              .withQueryStringParameters(
                "typeOfBusiness" -> typeOfBusiness,
                "businessId"     -> businessId,
                "fromDate"       -> fromDate,
                "toDate"         -> toDate,
                "status"         -> status
              )
              .get())
          response.status shouldBe NOT_FOUND
          response.json shouldBe Json.toJson(NoObligationsFoundError)
        }
      }
    }
  }
}
