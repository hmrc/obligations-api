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

package v2.controllers.requestParsers.validators

import api.models.errors._
import support.UnitSpec
import v2.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRawData

class RetrieveCrystallisationObligationsValidatorSpec extends UnitSpec {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2018-19"

  val validator = new RetrieveCrystallisationObligationsValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrieveCrystallisationObligationsRawData(validNino, Some(validTaxYear))) shouldBe Nil
      }
      "a valid request is supplied with no taxYear" in {
        validator.validate(RetrieveCrystallisationObligationsRawData(validNino, None)) shouldBe Nil
      }
      "a valid request is supplied with the earliest possible taxYear" in {
        validator.validate(RetrieveCrystallisationObligationsRawData(validNino, Some("2017-18"))) shouldBe Nil
      }
    }

    def test(nino: String, taxYear: String, error: MtdError): Unit = {
      s"return ${error.code} error" when {
        s"RetrieveCrystallisationObligationsRawData($nino, $taxYear) is supplied" in {
          validator.validate(RetrieveCrystallisationObligationsRawData(nino, Some(taxYear))) shouldBe List(error)
        }
      }
    }

    Seq(
      ("A12344A", validTaxYear, NinoFormatError),
      (validNino, "201-20", TaxYearFormatError),
      (validNino, "2016-17", RuleTaxYearNotSupportedError),
      (validNino, "2018-20", RuleTaxYearRangeExceededError),
    ).foreach(args => (test _).tupled(args))

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator.validate(RetrieveCrystallisationObligationsRawData("A12344A", Some("20178"))) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }
}
