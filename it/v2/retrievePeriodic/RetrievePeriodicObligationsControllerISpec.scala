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

package v2.retrievePeriodic

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v2.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrievePeriodicObligationsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino           = "AA123456A"
    val typeOfBusiness = "self-employment"
    val businessId     = "XAIS12345678901"
    val fromDate       = "2019-01-01"
    val toDate         = "2019-06-06"
    val status         = "Open"

    val responseBody = Json.parse("""{
        |  "obligations": [
        |     {
        |       "typeOfBusiness": "self-employment",
        |       "businessId": "XAIS12345678901",
        |       "obligationDetails": [
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
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
        |                "referenceNumber": "XAIS12345678901",
        |                "referenceType": "MTDBIS"
        |            },
        |            "obligationDetails": [
        |                {
        |                    "status": "O",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-06-06",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#001"
        |                }
        |            ]
        |        }
        |    ]
        |}
        |""".stripMargin
    )

    val responseBodyOneObjectMultipleDetails = Json.parse("""{
        |  "obligations": [
        |     {
        |       "typeOfBusiness": "self-employment",
        |       "businessId": "XAIS12345678901",
        |       "obligationDetails": [
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
        |           "status": "Open"
        |         },
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
        |           "receivedDate": "2019-04-25",
        |           "status": "Fulfilled"
        |         }
        |       ]
        |    }
        |  ]
        |}
        |""".stripMargin)

    val desResponseOneObjectMultipleDetails: JsValue = Json.parse(
      """
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
        |                    "status": "O",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-06-06",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#001"
        |                },
        |                {
        |                    "status": "F",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-06-06",
        |                    "inboundCorrespondenceDateReceived": "2019-04-25",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#002"
        |                }
        |            ]
        |        }
        |    ]
        |}
        |""".stripMargin
    )

    val responseBodyMultipleObjectsOneDetail = Json.parse("""{
        |  "obligations": [
        |     {
        |       "typeOfBusiness": "self-employment",
        |       "businessId": "XAIS12345678901",
        |       "obligationDetails": [
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
        |           "receivedDate": "2019-04-25",
        |           "status": "Open"
        |         }
        |       ]
        |    },
        |    {
        |       "typeOfBusiness": "self-employment",
        |       "businessId": "XAIS12345678901",
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

    val desResponseMultipleObjectsOneDetail: JsValue = Json.parse(
      """
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
        |                    "status": "O",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-06-06",
        |                    "inboundCorrespondenceDateReceived": "2019-04-25",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#001"
        |                }
        |            ]
        |        },
        |        {
        |            "identification": {
        |                "incomeSourceType": "ITSB",
        |                "referenceNumber": "XAIS12345678901",
        |                "referenceType": "MTDBIS"
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

    val responseBodyMultipleObjectsMultipleDetails = Json.parse("""{
        |  "obligations": [
        |     {
        |       "typeOfBusiness": "self-employment",
        |       "businessId": "XAIS12345678901",
        |       "obligationDetails": [
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
        |           "receivedDate": "2019-04-25",
        |           "status": "Open"
        |         },
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
        |           "receivedDate": "2019-04-25",
        |           "status": "Fulfilled"
        |         }
        |       ]
        |    },
        |    {
        |       "typeOfBusiness": "self-employment",
        |       "businessId": "XAIS12345678901",
        |       "obligationDetails": [
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
        |           "receivedDate": "2019-04-25",
        |           "status": "Open"
        |         },
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
        |           "receivedDate": "2019-04-25",
        |           "status": "Fulfilled"
        |         }
        |       ]
        |    }
        |  ]
        |}
        |""".stripMargin)

    val desResponseMultipleObjectsMultipleDetails: JsValue = Json.parse(
      """
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
        |                    "status": "O",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-06-06",
        |                    "inboundCorrespondenceDateReceived": "2019-04-25",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#001"
        |                },
        |                {
        |                    "status": "F",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-06-06",
        |                    "inboundCorrespondenceDateReceived": "2019-04-25",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#001"
        |                }
        |            ]
        |        },
        |        {
        |            "identification": {
        |                "incomeSourceType": "ITSB",
        |                "referenceNumber": "XAIS12345678901",
        |                "referenceType": "MTDBIS"
        |            },
        |            "obligationDetails": [
        |                {
        |                    "status": "O",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-06-06",
        |                    "inboundCorrespondenceDateReceived": "2019-04-25",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#001"
        |                },
        |                {
        |                    "status": "F",
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

    def setupStubs(): StubMapping

    def desUri: String = s"/enterprise/obligation-data/nino/$nino/ITSA"

    def queryParams: Map[String, String] = Map(
      "from" -> "2019-01-01",
      "to"   -> "2019-06-06"
    )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def uri: String = s"/$nino/income-and-expenditure"

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

      "a request with one object with one obligationDetail is made" in new Test {

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
              "status"         -> status)
            .get())

        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
      "a request with one object with multiple obligationDetails is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponseOneObjectMultipleDetails)
        }

        val response: WSResponse = await(
          request()
            .withQueryStringParameters(
              "typeOfBusiness" -> typeOfBusiness,
              "businessId"     -> businessId,
              "fromDate"       -> fromDate,
              "toDate"         -> toDate,
              "status"         -> status)
            .get())

        response.status shouldBe OK
        response.json shouldBe responseBodyOneObjectMultipleDetails
        response.header("Content-Type") shouldBe Some("application/json")
      }
      "a request with multiple objects with one obligationDetail is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponseMultipleObjectsOneDetail)
        }

        val response: WSResponse = await(
          request()
            .withQueryStringParameters(
              "typeOfBusiness" -> typeOfBusiness,
              "businessId"     -> businessId,
              "fromDate"       -> fromDate,
              "toDate"         -> toDate,
              "status"         -> status)
            .get())

        response.status shouldBe OK
        response.json shouldBe responseBodyMultipleObjectsOneDetail
        response.header("Content-Type") shouldBe Some("application/json")
      }
      "a request with multiple objects with multiple obligationDetails is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponseMultipleObjectsMultipleDetails)
        }

        val response: WSResponse = await(
          request()
            .withQueryStringParameters(
              "typeOfBusiness" -> typeOfBusiness,
              "businessId"     -> businessId,
              "fromDate"       -> fromDate,
              "toDate"         -> toDate,
              "status"         -> status)
            .get())

        response.status shouldBe OK
        response.json shouldBe responseBodyMultipleObjectsMultipleDetails
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "missing parameter error" when {

        "fromDate is missing" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
          }

          val response: WSResponse = await(
            request()
              .withQueryStringParameters("typeOfBusiness" -> typeOfBusiness, "businessId" -> businessId, "toDate" -> toDate, "status" -> status)
              .get())

          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(MissingFromDateError)
        }

        "toDate is missing" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
          }

          val response: WSResponse = await(
            request()
              .withQueryStringParameters("typeOfBusiness" -> typeOfBusiness, "businessId" -> businessId, "fromDate" -> fromDate, "status" -> status)
              .get())

          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(MissingToDateError)
        }

        "typeOfBusiness is missing while there is an businessId" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
          }

          val response: WSResponse = await(
            request().withQueryStringParameters("businessId" -> businessId, "fromDate" -> fromDate, "toDate" -> toDate, "status" -> status).get())

          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(MissingTypeOfBusinessError)
        }
      }

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTypeOfBusiness: String,
                                requestBusinessId: String,
                                requestFromDate: String,
                                requestToDate: String,
                                requestStatus: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String           = requestNino
            override val typeOfBusiness: String = requestTypeOfBusiness
            override val businessId: String     = requestBusinessId
            override val fromDate: String       = requestFromDate
            override val toDate: String         = requestToDate
            override val status: String         = requestStatus

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(
              request()
                .withQueryStringParameters(
                  "typeOfBusiness" -> typeOfBusiness,
                  "businessId"     -> businessId,
                  "fromDate"       -> fromDate,
                  "toDate"         -> toDate,
                  "status"         -> status)
                .get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA1", "self-employment", "XAIS12345678901", "2019-01-01", "2019-06-06", "Open", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "self-employment", "XAI", "2019-01-01", "2019-06-06", "Open", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-01", "2019-06-06", "Open", BAD_REQUEST, FromDateFormatError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-01-01", "2019-06", "Open", BAD_REQUEST, ToDateFormatError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-01-01", "2019-06-06", "Closed", BAD_REQUEST, StatusFormatError),
          ("AA123456A", "do-not-use", "XAIS12345678901", "2019-01-01", "2019-06-06", "Open", BAD_REQUEST, TypeOfBusinessFormatError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-06-06", "2019-01-01", "Open", BAD_REQUEST, ToDateBeforeFromDateError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2017-01-01", "2018-01-01", "Open", BAD_REQUEST, RuleFromDateNotSupportedError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-01-01", "2020-06-06", "Open", BAD_REQUEST, RuleDateRangeInvalidError)
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
                  "status"         -> status)
                .get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_STATUS", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_REGIME", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, FromDateFormatError),
          (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, ToDateFormatError),
          (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, RuleDateRangeInvalidError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (FORBIDDEN, "INSOLVENT_TRADER", BAD_REQUEST, RuleInsolventTraderError),
          (FORBIDDEN, "NOT_FOUND_BPKEY", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }

      "no obligation error" when {
        "no selected typeOfBusiness is found within the response object" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onSuccess(DesStub.GET, desUri, queryParams, OK, desResponse)
          }

          val response: WSResponse = await(
            request()
              .withQueryStringParameters(
                "typeOfBusiness" -> "uk-property",
                "businessId"     -> businessId,
                "fromDate"       -> fromDate,
                "toDate"         -> toDate,
                "status"         -> status)
              .get())

          response.status shouldBe NOT_FOUND
          response.json shouldBe Json.toJson(NoObligationsFoundError)
        }

        "no selected businessId is found within the response object" in new Test {

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
                "businessId"     -> "XAIS12345678903",
                "fromDate"       -> fromDate,
                "toDate"         -> toDate,
                "status"         -> status)
              .get())

          response.status shouldBe NOT_FOUND
          response.json shouldBe Json.toJson(NoObligationsFoundError)
        }
      }
    }
  }

}
