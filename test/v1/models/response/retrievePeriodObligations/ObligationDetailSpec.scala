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
import v1.models.domain.status.MtdStatus

class ObligationDetailSpec extends UnitSpec {
  "writes" should {
    "write to JSON" when {
      "passed a model with status Fulfilled" in {
        val json = Json.parse(
          """
            |{
            |    "periodStartDate": "2019-01-01",
            |    "periodEndDate": "2019-03-31",
            |    "dueDate": "2019-04-30",
            |    "receivedDate": "2019-04-25",
            |    "status": "Fulfilled"
            |}
            |""".stripMargin)
        val model = ObligationDetail("2019-01-01", "2019-03-31", "2019-04-30", Some("2019-04-25"), MtdStatus.Fulfilled)

        Json.toJson(model) shouldBe json
      }
      "passed a model with status Open" in {
        val json = Json.parse(
          """
            |{
            |    "periodStartDate": "2019-01-01",
            |    "periodEndDate": "2019-03-31",
            |    "dueDate": "2019-04-30",
            |    "receivedDate": "2019-04-25",
            |    "status": "Open"
            |}
            |""".stripMargin)
        val model = ObligationDetail("2019-01-01", "2019-03-31", "2019-04-30", Some("2019-04-25"), MtdStatus.Open)

        Json.toJson(model) shouldBe json
      }
      "passed a model with no receivedDate" in {
        val json = Json.parse(
          """
            |{
            |    "periodStartDate": "2019-01-01",
            |    "periodEndDate": "2019-03-31",
            |    "dueDate": "2019-04-30",
            |    "status": "Fulfilled"
            |}
            |""".stripMargin)
        val model = ObligationDetail("2019-01-01", "2019-03-31", "2019-04-30", None, MtdStatus.Fulfilled)

        Json.toJson(model) shouldBe json
      }
    }
  }

}
