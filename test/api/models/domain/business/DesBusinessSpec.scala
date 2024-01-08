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

package api.models.domain.business

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class DesBusinessSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[DesBusiness](
    ("ITSP", DesBusiness.ITSP),
    ("ITSB", DesBusiness.ITSB),
    ("ITSF", DesBusiness.ITSF),
    ("ITSA", DesBusiness.ITSA)
  )

  "toMtd" should {
    Seq(
      (DesBusiness.ITSF, Some(MtdBusiness.`foreign-property`)),
      (DesBusiness.ITSB, Some(MtdBusiness.`self-employment`)),
      (DesBusiness.ITSP, Some(MtdBusiness.`uk-property`)),
      (DesBusiness.ITSA, None)
    ).foreach { case (desBusiness, mtdBusiness) =>
      s"convert $desBusiness to $mtdBusiness" in {
        desBusiness.toMtd shouldBe mtdBusiness
      }
    }
  }

}
