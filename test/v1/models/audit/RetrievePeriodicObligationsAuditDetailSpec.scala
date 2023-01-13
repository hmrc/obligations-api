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

class RetrievePeriodicObligationsAuditDetailSpec extends UnitSpec {

  val nino = "ZG903729C"
  val invalidNino = "notANino"
  val businessId = "XAIS123456789012"

  val validJson = Json.parse(
    s"""{
       |    "userType": "Agent",
       |    "agentReferenceNumber":"012345678",
       |    "nino": "$nino",
       |    "typeOfBusiness":"self-employment",
       |    "businessId": "XAIS123456789012",
       |    "fromDate": "2019-01-01",
       |    "toDate": "2019-01-02",
       |    "status": "Fulfilled",
       |    "X-CorrelationId": "a1e8057e-fbbc-47a8-a8b478d9f015c253",
       |    "response": {
       |      "httpStatus": 200,
       |      "body": {
       |      "obligations": [
       |        {
       |            "identification": {
       |                "incomeSourceType": "ITSB",
       |                "referenceNumber": "XAIS12345678910",
       |                "referenceType": "MTDBIS"
       |            },
       |            "obligationDetails": [
       |                {
       |                    "status": "F",
       |                    "inboundCorrespondenceFromDate": "2018-04-06",
       |                    "inboundCorrespondenceToDate": "2019-04-05",
       |                    "inboundCorrespondenceDateReceived": "2020-01-25",
       |                    "inboundCorrespondenceDueDate": "1920-01-31",
       |                    "periodKey": "#001"
       |                }
       |            ]
       |        }
       |    ]
       |}
       |  }
       |}""".stripMargin)

  val validBody = RetrievePeriodicObligationsAuditDetail(
    userType = "Agent",
    agentReferenceNumber = Some("012345678"),
    nino = nino,
    typeOfBusiness = Some("self-employment"),
    businessId = Some("XAIS123456789012"),
    fromDate = Some("2019-01-01"),
    toDate = Some("2019-01-02"),
    status = Some("Fulfilled"),
    `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b478d9f015c253",
    response = AuditResponse(
      200,
      Right(Some(Json.parse(
        """
          |{
          |    "obligations": [
          |        {
          |            "identification": {
          |                "incomeSourceType": "ITSB",
          |                "referenceNumber": "XAIS12345678910",
          |                "referenceType": "MTDBIS"
          |            },
          |            "obligationDetails": [
          |                {
          |                    "status": "F",
          |                    "inboundCorrespondenceFromDate": "2018-04-06",
          |                    "inboundCorrespondenceToDate": "2019-04-05",
          |                    "inboundCorrespondenceDateReceived": "2020-01-25",
          |                    "inboundCorrespondenceDueDate": "1920-01-31",
          |                    "periodKey": "#001"
          |                }
          |            ]
          |        }
          |    ]
          |}
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
