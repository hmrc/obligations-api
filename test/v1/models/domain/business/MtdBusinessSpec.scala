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

package v1.models.domain.business

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class MtdBusinessSpec extends UnitSpec with EnumJsonSpecSupport{

  testRoundTrip[MtdBusiness](
    ("self-employment", MtdBusiness.`self-employment`),
    ("uk-property", MtdBusiness.`uk-property`),
    ("foreign-property", MtdBusiness.`foreign-property`),
    ("do-not-use", MtdBusiness.`do-not-use`)
  )

  "toDes" should {
    Seq((DesBusiness.ITSF, MtdBusiness.`foreign-property`),
      (DesBusiness.ITSB, MtdBusiness.`self-employment`),
      (DesBusiness.ITSP, MtdBusiness.`uk-property`),
      (DesBusiness.ITSA, MtdBusiness.`do-not-use`)).foreach {
      case (desBusiness, mtdBusiness) =>
        s"convert $mtdBusiness to $desBusiness" in {
          mtdBusiness.toDes shouldBe desBusiness
        }
    }
  }
}
