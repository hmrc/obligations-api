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

package v1.models.audit

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.auth.UserDetails

class RetrieveCrystallisationObligationsAuditDetailSpec extends UnitSpec {

  val nino = "ZG903729C"
  val invalidNino = "notANino"
  val businessId = "XAIS123456789012"

  val validJson = Json.parse(
    s"""{
       |    "userType": "Agent",
       |    "agentReferenceNumber":"012345678",
       |    "nino": "$nino",
       |    "taxYear": "2019-20",
       |    "X-CorrelationId": "a1e8057e-fbbc-47a8-a8b478d9f015c253",
       |    "response": {
       |      "httpStatus": 200,
       |      "body": {
       |    "status": "Fulfilled",
       |    "periodStartDate": "2018-04-06",
       |    "periodEndDate": "2019-04-05",
       |    "receivedDate": "2020-01-25",
       |    "dueDate": "1920-01-31"
       |    }
       |  }
       |}""".stripMargin)

  val validBody = RetrieveCrystallisationObligationsAuditDetail(
    userDetails = UserDetails("id", "Agent", Some("012345678")),
    nino = nino,
    taxYear = Some("2019-20"),
    `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b478d9f015c253",
    auditResponse = AuditResponse(
      200,
      Right(Some(Json.parse(
        """
          |     {
          |    "status": "Fulfilled",
          |    "periodStartDate": "2018-04-06",
          |    "periodEndDate": "2019-04-05",
          |    "receivedDate": "2020-01-25",
          |    "dueDate": "1920-01-31"
          |    }
          |""".stripMargin)))
    )
  )


  "writes" must {
    "work" when {
      "success response" in {
        Json.toJson(validBody) shouldBe validJson
      }
    }
  }
}
