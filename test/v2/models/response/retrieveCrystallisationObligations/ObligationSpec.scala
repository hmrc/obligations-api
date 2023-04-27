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
import v2.fixtures.RetrieveCrystallisationObligationsFixtures._

class ObligationSpec extends UnitSpec {

  "writes" should {
    "write to JSON" when {
      "passed a model with status Fulfilled" in {
        Json.toJson(mtdObligationModel(status = MtdStatus.Fulfilled)) shouldBe mtdObligationJson("Fulfilled")
      }

      "passed a model with status Open" in {
        Json.toJson(mtdObligationModel(status = MtdStatus.Open)) shouldBe mtdObligationJson("Open")
      }

      "passed a model with no receivedDate" in {
        Json.toJson(mtdObligationModel(receivedDate = None)) shouldBe mtdObligationJsonNoReceivedDate
      }
    }
  }
}
