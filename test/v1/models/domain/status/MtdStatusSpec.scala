/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.domain.status

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class MtdStatusSpec extends UnitSpec with EnumJsonSpecSupport {
  testRoundTrip[MtdStatus](
    ("Fulfilled", MtdStatus.Fulfilled),
    ("Open", MtdStatus.Open)
  )

  "toDes" should {
    Seq((DesStatus.F, MtdStatus.Fulfilled), (DesStatus.O, MtdStatus.Open)).foreach {
      case (desStatus, mtdStatus) =>
        s"convert $mtdStatus to $desStatus" in {
          mtdStatus.toDes shouldBe desStatus
        }
    }
  }
}
