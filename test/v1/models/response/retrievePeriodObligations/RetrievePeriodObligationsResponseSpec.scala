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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.models.response.retrievePeriodObligations

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class RetrievePeriodObligationsResponseSpec extends UnitSpec with JsonErrorValidators {

  val obligationDetails = Obligation(Some("18AD"), "2018-04-06", "2019-04-05", "2018-04-06", Some("2019-12-15"), "Open")
  val businessDetails = Business("self-employment", "SomeID", Seq(obligationDetails))
  val responseBody = RetrievePeriodObligationsResponse(Seq(businessDetails))

  "reads" when {
    "passed valid JSON" should {
      val inputJson = Json.parse(
        """
          |{
          |    "obligations": [
          |        {
          |            "identification": {
          |                "incomeSourceType": "ITSB",
          |                "incomeSourceId": "SomeID",
          |                "referenceType": "IncomeSourceId"
          |            },
          |            "obligationDetails": [
          |                {
          |                    "status": "O",
          |                    "inboundCorrespondenceFromDate": "2018-04-06",
          |                    "inboundCorrespondenceToDate": "2019-04-05",
          |                    "inboundCorrespondenceDateReceived": "2019-12-15",
          |                    "inboundCorrespondenceDueDate": "2018-04-06",
          |                    "periodKey": "18AD"
          |                }
          |            ]
          |        }
          |    ]
          |}
        """.stripMargin
      )
      "return a valid model" in {
        responseBody shouldBe inputJson.as[RetrievePeriodObligationsResponse]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      val resJson = Json.parse(
        """
          |{
          |  "obligations": [
          |     {
          |       "typeOfBusiness": "self-employment",
          |       "incomeSourceId": "SomeID",
          |       "obligationDetails": [
          |         {
          |           "periodKey": "18AD",
          |           "fromDate": "2018-04-06",
          |           "toDate": "2019-04-05",
          |           "dueDate": "2018-04-06",
          |           "receivedDate": "2019-12-15",
          |           "status": "Open"
          |         }
          |       ]
          |    }
          |  ]
          |}
          |""".stripMargin
      )
      "return valid json" in {
        Json.toJson(responseBody) shouldBe resJson
      }
    }
  }


}
