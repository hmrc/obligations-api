/*
 * Copyright 2026 HM Revenue & Customs
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

import api.models.errors.*
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class RetrievePeriodicObligationsControllerHipISpec extends IntegrationBaseSpec {

  "Calling the retrieve periodic obligations endpoint" should {

    "return a 200 status code" when {

      "a request with one object with one obligationDetail is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, hipResponse)
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
          AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, hipResponseOneObjectMultipleDetails)
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
          AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, hipResponseMultipleObjectsOneDetail)
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
          AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, hipResponseMultipleObjectsMultipleDetails)
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
            AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, hipResponse)
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
            AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, hipResponse)
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
            AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, hipResponse)
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
              AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
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
          ("AA1", "self-employment", "XAIS12345678901", "2019-01-01", "2019-06-06", "open", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "self-employment", "XAI", "2019-01-01", "2019-06-06", "open", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-01", "2019-06-06", "open", BAD_REQUEST, FromDateFormatError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-01-01", "2019-06", "open", BAD_REQUEST, ToDateFormatError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-01-01", "2019-06-06", "Closed", BAD_REQUEST, StatusFormatError),
          ("AA123456A", "do-not-use", "XAIS12345678901", "2019-01-01", "2019-06-06", "open", BAD_REQUEST, TypeOfBusinessFormatError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-06-06", "2019-01-01", "open", BAD_REQUEST, ToDateBeforeFromDateError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2017-01-01", "2018-01-01", "open", BAD_REQUEST, RuleFromDateNotSupportedError),
          ("AA123456A", "self-employment", "XAIS12345678901", "2019-01-01", "2020-06-06", "open", BAD_REQUEST, RuleDateRangeInvalidError)
        )
        input.foreach(validationErrorTest.tupled)
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))
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
          (BAD_REQUEST, "001", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "041", BAD_REQUEST, RuleDateRangeInvalidError),
          (NOT_FOUND, "002", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "025", NOT_FOUND, NotFoundError),
          (FORBIDDEN, "094", BAD_REQUEST, RuleInsolventTraderError),
          (UNPROCESSABLE_ENTITY, "042", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "ANY_SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(serviceErrorTest.tupled)
      }

      "no obligation error" when {
        "no selected typeOfBusiness is found within the response object" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, hipResponse)
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
            AuthStub.authorisedWithIndividualAffinityGroupAndEnrolment()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, hipResponse)
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

  private trait Test {

    val nino           = "AA123456A"
    val typeOfBusiness = "self-employment"
    val businessId     = "XAIS12345678901"
    val fromDate       = "2019-01-01"
    val toDate         = "2019-06-06"
    val status         = "open"

    val responseBody: JsValue = Json.parse(
      """
        |{
        |  "obligations": [
        |    {
        |      "typeOfBusiness": "self-employment",
        |      "businessId": "XAIS12345678901",
        |      "obligationDetails": [
        |        {
        |          "periodStartDate": "2019-01-01",
        |          "periodEndDate": "2019-06-06",
        |          "dueDate": "2019-04-30",
        |          "status": "open"
        |        }
        |      ]
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val hipResponse: JsValue = Json.parse(
      """
        |{
        |  "success": {
        |    "obligations": [
        |      {
        |        "identification": {
        |          "incomeSourceType": "ITSB",
        |          "referenceNumber": "XAIS12345678901",
        |          "referenceType": "MTDBIS"
        |        },
        |        "obligationDetails": [
        |          {
        |            "status": "O",
        |            "inboundCorrespondenceFromDate": "2019-01-01",
        |            "inboundCorrespondenceToDate": "2019-06-06",
        |            "inboundCorrespondenceDueDate": "2019-04-30",
        |            "periodKey": "#001"
        |          }
        |        ]
        |      }
        |    ]
        |  }
        |}
      """.stripMargin
    )

    val responseBodyOneObjectMultipleDetails: JsValue = Json.parse(
      """
        |{
        |  "obligations": [
        |     {
        |       "typeOfBusiness": "self-employment",
        |       "businessId": "XAIS12345678901",
        |       "obligationDetails": [
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
        |           "status": "open"
        |         },
        |         {
        |           "periodStartDate": "2019-01-01",
        |           "periodEndDate": "2019-06-06",
        |           "dueDate": "2019-04-30",
        |           "receivedDate": "2019-04-25",
        |           "status": "fulfilled"
        |         }
        |       ]
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val hipResponseOneObjectMultipleDetails: JsValue = Json.parse(
      """
        |{
        |  "success": {
        |    "obligations": [
        |      {
        |        "identification": {
        |          "incomeSourceType": "ITSB",
        |          "referenceNumber": "XAIS12345678901",
        |          "referenceType": "MTDBIS"
        |        },
        |        "obligationDetails": [
        |          {
        |            "status": "O",
        |            "inboundCorrespondenceFromDate": "2019-01-01",
        |            "inboundCorrespondenceToDate": "2019-06-06",
        |            "inboundCorrespondenceDueDate": "2019-04-30",
        |            "periodKey": "#001"
        |          },
        |          {
        |            "status": "F",
        |            "inboundCorrespondenceFromDate": "2019-01-01",
        |            "inboundCorrespondenceToDate": "2019-06-06",
        |            "inboundCorrespondenceDateReceived": "2019-04-25",
        |            "inboundCorrespondenceDueDate": "2019-04-30",
        |            "periodKey": "#002"
        |          }
        |        ]
        |      }
        |    ]
        |  }
        |}
      """.stripMargin
    )

    val responseBodyMultipleObjectsOneDetail: JsValue = Json.parse(
      """
        |{
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
        |           "status": "open"
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
        |           "status": "open"
        |         }
        |       ]
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val hipResponseMultipleObjectsOneDetail: JsValue = Json.parse(
      """
        |{
        |  "success": {
        |    "obligations": [
        |      {
        |        "identification": {
        |          "incomeSourceType": "ITSB",
        |          "referenceNumber": "XAIS12345678901",
        |          "referenceType": "MTDBIS"
        |        },
        |        "obligationDetails": [
        |          {
        |            "status": "O",
        |            "inboundCorrespondenceFromDate": "2019-01-01",
        |            "inboundCorrespondenceToDate": "2019-06-06",
        |            "inboundCorrespondenceDateReceived": "2019-04-25",
        |            "inboundCorrespondenceDueDate": "2019-04-30",
        |            "periodKey": "#001"
        |          }
        |        ]
        |      },
        |      {
        |        "identification": {
        |          "incomeSourceType": "ITSB",
        |          "referenceNumber": "XAIS12345678901",
        |          "referenceType": "MTDBIS"
        |        },
        |        "obligationDetails": [
        |          {
        |            "status": "O",
        |            "inboundCorrespondenceFromDate": "2019-01-01",
        |            "inboundCorrespondenceToDate": "2019-06-06",
        |            "inboundCorrespondenceDateReceived": "2019-04-25",
        |            "inboundCorrespondenceDueDate": "2019-04-30",
        |            "periodKey": "#001"
        |          }
        |        ]
        |      }
        |    ]
        |  }
        |}
      """.stripMargin
    )

    val responseBodyMultipleObjectsMultipleDetails: JsValue = Json.parse(
      """
        |{
        |  "obligations": [
        |    {
        |      "typeOfBusiness": "self-employment",
        |      "businessId": "XAIS12345678901",
        |      "obligationDetails": [
        |        {
        |          "periodStartDate": "2019-01-01",
        |          "periodEndDate": "2019-06-06",
        |          "dueDate": "2019-04-30",
        |          "receivedDate": "2019-04-25",
        |          "status": "open"
        |        },
        |        {
        |          "periodStartDate": "2019-01-01",
        |          "periodEndDate": "2019-06-06",
        |          "dueDate": "2019-04-30",
        |          "receivedDate": "2019-04-25",
        |          "status": "fulfilled"
        |        }
        |      ]
        |    },
        |    {
        |      "typeOfBusiness": "self-employment",
        |      "businessId": "XAIS12345678901",
        |      "obligationDetails": [
        |        {
        |          "periodStartDate": "2019-01-01",
        |          "periodEndDate": "2019-06-06",
        |          "dueDate": "2019-04-30",
        |          "receivedDate": "2019-04-25",
        |          "status": "open"
        |        },
        |        {
        |          "periodStartDate": "2019-01-01",
        |          "periodEndDate": "2019-06-06",
        |          "dueDate": "2019-04-30",
        |          "receivedDate": "2019-04-25",
        |          "status": "fulfilled"
        |        }
        |      ]
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val hipResponseMultipleObjectsMultipleDetails: JsValue = Json.parse(
      """
        |{
        |  "success": {
        |    "obligations": [
        |      {
        |        "identification": {
        |          "incomeSourceType": "ITSB",
        |          "referenceNumber": "XAIS12345678901",
        |          "referenceType": "MTDBIS"
        |        },
        |        "obligationDetails": [
        |          {
        |            "status": "O",
        |            "inboundCorrespondenceFromDate": "2019-01-01",
        |            "inboundCorrespondenceToDate": "2019-06-06",
        |            "inboundCorrespondenceDateReceived": "2019-04-25",
        |            "inboundCorrespondenceDueDate": "2019-04-30",
        |            "periodKey": "#001"
        |          },
        |          {
        |            "status": "F",
        |            "inboundCorrespondenceFromDate": "2019-01-01",
        |            "inboundCorrespondenceToDate": "2019-06-06",
        |            "inboundCorrespondenceDateReceived": "2019-04-25",
        |            "inboundCorrespondenceDueDate": "2019-04-30",
        |            "periodKey": "#001"
        |          }
        |        ]
        |      },
        |      {
        |        "identification": {
        |          "incomeSourceType": "ITSB",
        |          "referenceNumber": "XAIS12345678901",
        |          "referenceType": "MTDBIS"
        |        },
        |        "obligationDetails": [
        |          {
        |            "status": "O",
        |            "inboundCorrespondenceFromDate": "2019-01-01",
        |            "inboundCorrespondenceToDate": "2019-06-06",
        |            "inboundCorrespondenceDateReceived": "2019-04-25",
        |            "inboundCorrespondenceDueDate": "2019-04-30",
        |            "periodKey": "#001"
        |          },
        |          {
        |            "status": "F",
        |            "inboundCorrespondenceFromDate": "2019-01-01",
        |            "inboundCorrespondenceToDate": "2019-06-06",
        |            "inboundCorrespondenceDateReceived": "2019-04-25",
        |            "inboundCorrespondenceDueDate": "2019-04-30",
        |            "periodKey": "#001"
        |          }
        |        ]
        |      }
        |    ]
        |  }
        |}
      """.stripMargin
    )

    def setupStubs(): StubMapping

    def downstreamUri: String = s"/etmp/RESTAdapter/obligation-data/nino/$nino/ITSA"

    def downstreamQueryParams: Map[String, String] = Map(
      "dateFrom" -> "2019-01-01",
      "dateTo"   -> "2019-06-06"
    )

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    private def uri: String = s"/$nino/income-and-expenditure"

    def errorBody(code: String): String =
      s"""
         |{
         |  "errors": {
         |    "processingDate": "2022-01-31T09:26:17Z",
         |    "code": "$code",
         |    "text": "message from HIP"
         |  }
         |}
      """.stripMargin

  }

}
