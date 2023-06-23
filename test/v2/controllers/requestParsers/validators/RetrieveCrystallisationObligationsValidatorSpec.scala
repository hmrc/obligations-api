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
  private val validStatus  = "Open"

  val validator = new RetrieveCrystallisationObligationsValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        val result = validator.validate(RetrieveCrystallisationObligationsRawData(validNino, Some(validTaxYear), Some(validStatus)))
        result shouldBe Nil
      }
      "a valid request is supplied with no taxYear" in {
        val result = validator.validate(RetrieveCrystallisationObligationsRawData(validNino, None, Some(validStatus)))
        result shouldBe Nil
      }
      "a valid request is supplied with the earliest possible taxYear" in {
        val result = validator.validate(RetrieveCrystallisationObligationsRawData(validNino, Some("2017-18"), Some(validStatus)))
        result shouldBe Nil
      }
      "a valid request is supplied with no status" in {
        val result = validator.validate(RetrieveCrystallisationObligationsRawData(validNino, Some(validTaxYear), None))
        result shouldBe Nil
      }
    }

    def test(nino: String, taxYear: String, status: String, error: MtdError): Unit = {
      s"return ${error.code} error" when {
        s"RetrieveCrystallisationObligationsRawData($nino, $taxYear) is supplied" in {
          validator.validate(RetrieveCrystallisationObligationsRawData(nino, Some(taxYear), Some(status))) shouldBe List(error)
        }
      }
    }

    List(
      ("A12344A", validTaxYear, validStatus, NinoFormatError),
      (validNino, "201-20", validStatus, TaxYearFormatError),
      (validNino, "2016-17", validStatus, RuleTaxYearNotSupportedError),
      (validNino, "2018-20", validStatus, RuleTaxYearRangeInvalidError),
      (validNino, validTaxYear, "OPENN", StatusFormatError),
    ).foreach(args => (test _).tupled(args))

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result = validator.validate(RetrieveCrystallisationObligationsRawData("A12344A", Some("20178"), Some("NOT-A-STATUS")))
        result shouldBe List(NinoFormatError, TaxYearFormatError, StatusFormatError)
      }
    }
  }
}
