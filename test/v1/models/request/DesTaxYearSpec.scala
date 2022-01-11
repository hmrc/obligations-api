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

package v1.models.request

import support.UnitSpec

class DesTaxYearSpec extends UnitSpec {

  val desTaxYear = DesTaxYear("2018")

  "DesTaxYear" should {
    "return the des taxYear" when {
      "a valid mtd taxYear is supplied" in {
        DesTaxYear.fromMtd("2017-18") shouldBe DesTaxYear("2018")
      }
    }
    "return the year as a string" when {
      "the toString function is used on the object" in {
        desTaxYear.toString shouldBe "2018"
      }
    }
  }
}
