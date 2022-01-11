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

import java.time.LocalDate

import support.UnitSpec

class ObligationsTaxYearSpec extends UnitSpec {

  trait Test extends ObligationsTaxYearHelpers

  "RawTaxYear" should {
    "return a RawTaxYear model" when {
      "passed a valid taxYear" in new Test {
        RawTaxYear(Some("2019-20")) shouldBe RawTaxYear(Some("2019-20"))
      }
      "passed no taxYear" in new Test {
        RawTaxYear(None) shouldBe RawTaxYear(None)
      }
    }
    "return an exception" when {
      "passed an invalid taxYear" in new Test {
        val result = intercept[IllegalArgumentException](RawTaxYear(Some("201-20")))
        result.getMessage shouldBe "requirement failed"
      }
    }
  }

  "RawTaxYear.toObligationsTaxYear" should {
    "return an ObligationsTaxYear" when {
      "a valid taxYear is entered" in new Test {
        RawTaxYear(Some("2019-20")).toObligationsTaxYear shouldBe ObligationsTaxYear("2019-04-06", "2020-04-05")
      }
      "no taxYear is entered" in new Test {
        override val date: LocalDate = LocalDate.parse("2020-04-06")
        RawTaxYear(None).toObligationsTaxYear shouldBe ObligationsTaxYear("2019-04-06", "2020-04-05")
      }
    }
    "return an exception" when {
      "passed an invalid taxYear" in new Test {
        val result = intercept[IllegalArgumentException](RawTaxYear(Some("201-20")).toObligationsTaxYear)
        result.getMessage shouldBe "requirement failed"
      }
    }
  }

  "mostRecentTaxYear" should {
    "return fromMtd(2018-19)" when {
      "passed a date in the tax year 2019-20 before 04-05" in new Test {
        override val date: LocalDate = LocalDate.parse("2020-02-06")
        getMostRecentTaxYear shouldBe "2018-19"
      }
      "passed the last date in 2019-20 (2020-04-05)" in new Test {
        override val date: LocalDate = LocalDate.parse("2020-04-05")
        getMostRecentTaxYear shouldBe "2018-19"
      }
    }

    "return fromMtd(2019-20)" when {
      "passed a date after 2020-04-05" in new Test {
        override val date: LocalDate = LocalDate.parse("2020-04-06")
        getMostRecentTaxYear shouldBe  "2019-20"
      }
    }
  }
}
