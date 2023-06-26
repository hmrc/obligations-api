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

package v2.models.response.retrieveCrystallisationObligations

import api.models.domain.status.MtdStatus
import play.api.libs.json.Json
import support.UnitSpec
import v2.fixtures.RetrieveCrystallisationObligationsFixtures.{ mtdObligationJson, mtdObligationJsonNoReceivedDate, mtdObligationModel }

class RetrieveCrystallisationObligationsResponseSpec extends UnitSpec {

  "writes" should {
    "write to JSON" when {
      "passed a response with multiple obligations" in {
        val json = Json.parse(s"""
             |{
             |    "obligations": [
             |        ${mtdObligationJson("Fulfilled")},
             |        ${mtdObligationJson("Open")},
             |        $mtdObligationJsonNoReceivedDate
             |    ]
             |}
             |""".stripMargin)

        val model = RetrieveCrystallisationObligationsResponse(
          List(
            mtdObligationModel(status = MtdStatus.Fulfilled),
            mtdObligationModel(status = MtdStatus.Open),
            mtdObligationModel(receivedDate = None),
          ))

        Json.toJson(model) shouldBe json
      }
    }
  }
}
