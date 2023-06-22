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

package api.models.domain

import support.UnitSpec

import java.time.LocalDate

class TaxYearSpec extends UnitSpec {

  "TaxYear" when {

    val taxYear = TaxYear.fromMtd("2023-24")

    "constructed from an MTD tax year" should {
      "return the downstream tax value" in {
        taxYear.asDownstream shouldBe "2024"
      }

      "return the MTD tax year" in {
        taxYear.asMtd shouldBe "2023-24"
      }

      "return the tax year in the 'Tax Year Specific API' format" in {
        taxYear.asTysDownstream shouldBe "23-24"
      }
    }

    "constructed from a downstream tax year" should {
      "return the downstream tax value" in {
        TaxYear.fromDownstream("2019").asDownstream shouldBe "2019"
      }

      "allow the MTD tax year to be extracted" in {
        TaxYear.fromDownstream("2019").asMtd shouldBe "2018-19"
      }
    }

    "constructed directly" should {
      "not compile" in {
        """new TaxYear("2021-22")""" shouldNot compile
      }
    }

    "compared with equals" should {
      "have equality based on content" in {
        val taxYear = TaxYear.fromMtd("2021-22")
        taxYear shouldBe TaxYear.fromDownstream("2022")
        taxYear should not be TaxYear.fromDownstream("2021")
      }
    }
  }

  "currentTaxYear()" should {
    "return the current tax year" when {
      def test(date: String): TaxYear = {
        implicit val todaySupplier: () => LocalDate = () => LocalDate.parse(date)
        TaxYear.currentTaxYear()
      }

      "today is before April" in {
        val result = test("2023-03-01")
        result.asMtd shouldBe "2022-23"
      }

      "today is April but before the 6th" in {
        val result = test("2023-04-05")
        result.asMtd shouldBe "2022-23"
      }

      "today is April 6" in {
        val result = test("2023-04-06")
        result.asMtd shouldBe "2023-24"
      }

      "today is after April 6" in {
        val result = test("2023-05-15")
        result.asMtd shouldBe "2023-24"
      }
    }
  }

  "minimumTaxYear" should {
    "be a TaxYear with the expected year-start and year-end dates" in {
      val minimumTaxYear = TaxYear.minimumTaxYear
      val year           = minimumTaxYear.year.toString
      year.length shouldBe 4
      minimumTaxYear.taxYearStart shouldBe "2017-04-06"
      minimumTaxYear.taxYearEnd shouldBe s"$year-04-05"
    }
  }

}
