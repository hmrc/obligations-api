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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.{ WSRequest, WSResponse }
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.stubs.{ AuditStub, AuthStub, DesStub, MtdIdLookupStub }

import java.time.Year

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino: String      = "AA123456A"
    val taxYearEnd: Int   = Year.now.getValue
    val taxYearStart: Int = Year.now.getValue - 1
    val taxYear: String   = s"$taxYearStart-${taxYearEnd.toString.drop(2)}"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/$nino/crystallisation")
        .addQueryStringParameters("taxYear" -> taxYear)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def desUri: String = s"/enterprise/obligation-data/nino/$nino/ITSA"

    val desResponse: JsValue = Json.parse("""
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
  }

  "Calling the crystallisation endpoint" when {

    "the NINO cannot be converted to a MTD ID" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.internalServerError(nino)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {

      "return 200" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Map("from" -> s"$taxYearStart-04-06", "to" -> s"$taxYearEnd-04-05"), Status.OK, desResponse)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT logged in" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.FORBIDDEN
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT authorised" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.FORBIDDEN
      }
    }
  }
}
