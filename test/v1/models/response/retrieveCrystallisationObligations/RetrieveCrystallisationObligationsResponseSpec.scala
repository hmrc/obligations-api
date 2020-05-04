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

package v1.models.response.retrieveCrystallisationObligations

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.domain.status.MtdStatus

class RetrieveCrystallisationObligationsResponseSpec extends UnitSpec {
  "writes" should {
    "write to JSON" when {
      "passed a model with status Fulfilled" in {
        val json = Json.parse(
          """
            |{
            |    "status": "Fulfilled",
            |    "periodStartDate": "2018-04-06",
            |    "periodEndDate": "2019-04-05",
            |    "receivedDate": "2020-01-25",
            |    "dueDate": "1920-01-31"
            |}
            |""".stripMargin)
        val model = RetrieveCrystallisationObligationsResponse("2018-04-06", "2019-04-05", "1920-01-31", MtdStatus.Fulfilled, Some("2020-01-25"))

        Json.toJson(model) shouldBe json
      }
      "passed a model with status Open" in {
        val json = Json.parse(
          """
            |{
            |    "status": "Open",
            |    "periodStartDate": "2018-04-06",
            |    "periodEndDate": "2019-04-05",
            |    "receivedDate": "2020-01-25",
            |    "dueDate": "1920-01-31"
            |}
            |""".stripMargin)
        val model = RetrieveCrystallisationObligationsResponse("2018-04-06", "2019-04-05", "1920-01-31", MtdStatus.Open, Some("2020-01-25"))

        Json.toJson(model) shouldBe json
      }
      "passed a model with no receivedDate" in {
        val json = Json.parse(
          """
            |{
            |    "status": "Open",
            |    "periodStartDate": "2018-04-06",
            |    "periodEndDate": "2019-04-05",
            |    "dueDate": "1920-01-31"
            |}
            |""".stripMargin)
        val model = RetrieveCrystallisationObligationsResponse("2018-04-06", "2019-04-05", "1920-01-31", MtdStatus.Open, None)

        Json.toJson(model) shouldBe json
      }
    }
  }
}
