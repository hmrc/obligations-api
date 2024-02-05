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

package v1.models.response.retrievePeriodicObligations

import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatus
import api.models.utils.JsonErrorValidators
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.response.common.{BusinessObligation, ObligationDetail}

class RetrievePeriodObligationsResponseSpec extends UnitSpec with JsonErrorValidators {

  "writes" should {
    "passed valid model" when {
      val resJson = Json.parse(
        """
            |{
            |  "obligations": [
            |     {
            |       "typeOfBusiness": "self-employment",
            |       "businessId": "XAIS12345678910",
            |       "obligationDetails": [
            |         {
            |           "periodStartDate": "2019-01-01",
            |           "periodEndDate": "2019-03-31",
            |           "dueDate": "2019-04-30",
            |           "receivedDate": "2019-04-25",
            |           "status": "Fulfilled"
            |         },
            |         {
            |           "periodStartDate": "2019-04-01",
            |           "periodEndDate": "2019-06-30",
            |           "dueDate": "2019-07-31",
            |           "receivedDate": "2019-07-01",
            |           "status": "Fulfilled"
            |         },
            |         {
            |           "periodStartDate": "2019-07-01",
            |           "periodEndDate": "2019-09-30",
            |           "dueDate": "2019-10-31",
            |           "receivedDate": "2019-10-08",
            |           "status": "Fulfilled"
            |         },
            |         {
            |           "periodStartDate": "2019-10-01",
            |           "periodEndDate": "2019-12-31",
            |           "dueDate": "2020-01-31",
            |           "status": "Open"
            |         }
            |       ]
            |    }
            |  ]
            |}
            |""".stripMargin
      )
      "return valid json" in {
        val responseBody = RetrievePeriodObligationsResponse(
          Seq(
            BusinessObligation(
              MtdBusiness.`self-employment`,
              "XAIS12345678910",
              Seq(
                ObligationDetail("2019-01-01", "2019-03-31", "2019-04-30", Some("2019-04-25"), MtdStatus.Fulfilled),
                ObligationDetail("2019-04-01", "2019-06-30", "2019-07-31", Some("2019-07-01"), MtdStatus.Fulfilled),
                ObligationDetail("2019-07-01", "2019-09-30", "2019-10-31", Some("2019-10-08"), MtdStatus.Fulfilled),
                ObligationDetail("2019-10-01", "2019-12-31", "2020-01-31", None, MtdStatus.Open)
              )
            )
          ))

        Json.toJson(responseBody) shouldBe resJson
      }
    }
  }

}
