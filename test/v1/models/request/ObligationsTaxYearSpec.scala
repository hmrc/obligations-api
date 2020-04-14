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

package v1.models.request

import java.time.LocalDate

import support.UnitSpec

class ObligationsTaxYearSpec extends UnitSpec {
  "fromMtd" should {
    "return an ObligationsTaxYear model" when {
      "passed a valid String" in {
        ObligationsTaxYear.fromMtd("2019-20") shouldBe ObligationsTaxYear("2019-04-06", "2020-04-05")
      }
    }
  }

  "mostRecentTaxYear" should {
    "return fromMtd(2018-19)" when {
      "passed a date in the tax year 2019-20 before 04-05" in {
        ObligationsTaxYear.mostRecentTaxYear(LocalDate.parse("2020-02-06")) shouldBe "2018-19"
      }
      "passed the last date in 2019-20 (2020-04-05)" in {
        ObligationsTaxYear.mostRecentTaxYear(LocalDate.parse("2020-04-05")) shouldBe "2018-19"
      }
    }

    "return fromMtd(2019-20)" when {
      "passed a date after 2020-04-05" in {
        ObligationsTaxYear.mostRecentTaxYear(LocalDate.parse("2020-04-06")) shouldBe  "2019-20"
      }
    }
  }
}
