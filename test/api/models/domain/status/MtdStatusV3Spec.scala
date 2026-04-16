/*
 * Copyright 2026 HM Revenue & Customs
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

package api.models.domain.status

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class MtdStatusV3Spec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[MtdStatusV3](
    ("fulfilled", MtdStatusV3.fulfilled),
    ("open", MtdStatusV3.open)
  )

  "toDes" should {
    Seq((DesStatusV3.F, MtdStatusV3.fulfilled), (DesStatusV3.O, MtdStatusV3.open)).foreach { case (desStatus, mtdStatus) =>
      s"convert $mtdStatus to $desStatus" in {
        mtdStatus.toDes shouldBe desStatus
      }
    }
  }

}
