/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.models.response.downstream

import api.models.domain.business.DesBusiness
import play.api.libs.json.Json
import support.UnitSpec

class DownstreamIdentificationSpec extends UnitSpec {

  "DownstreamIdentification" when {
    "read from JSON" must {
      "work when all fields are present" in {
        Json
          .parse("""{
              |  "incomeSourceType": "ITSA",
              |  "referenceNumber": "refNo",
              |  "referenceType": "refType"
              |}""".stripMargin)
          .as[DownstreamIdentification] shouldBe
          DownstreamIdentification(
            incomeSourceType = Some(DesBusiness.ITSA),
            referenceNumber = "refNo",
            referenceType = "refType"
          )
      }

      "work when optional fields are absent" in {
        Json
          .parse("""{
                   |  "referenceNumber": "refNo",
                   |  "referenceType": "refType"
                   |}""".stripMargin)
          .as[DownstreamIdentification] shouldBe
          DownstreamIdentification(
            incomeSourceType = None,
            referenceNumber = "refNo",
            referenceType = "refType"
          )
      }
    }
  }

}
