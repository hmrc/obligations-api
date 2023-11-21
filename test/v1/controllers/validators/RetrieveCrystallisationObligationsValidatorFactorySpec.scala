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

package v1.controllers.validators

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import support.UnitSpec
import v1.models.request.ObligationsTaxYear
import v1.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest

class RetrieveCrystallisationObligationsValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  val validatorFactory     = new RetrieveCrystallisationObligationsValidatorFactory()
  private val validNino    = "AA123456A"
  private val validTaxYear = "2018-19"

  private def validator(nino: String, taxYear: Option[String]) =
    validatorFactory.validator(nino, taxYear)

  "validator" when {
    "a valid request is supplied" must {
      "return the parsed domain object" in {
        validator(validNino, Some("2018-19")).validateAndWrapResult() shouldBe
          Right(RetrieveCrystallisationObligationsRequest(Nino(validNino), ObligationsTaxYear("2018-04-06", "2019-04-05")))
      }
    }

    "a valid request is supplied with no taxYear" must {
      "return the parsed domain object using the previous tax year" in {
        val taxYear = TaxYear.starting(TaxYear.currentTaxYear().startYear - 1)

        validator(validNino, None).validateAndWrapResult() shouldBe
          Right(
            RetrieveCrystallisationObligationsRequest(
              Nino(validNino),
              ObligationsTaxYear(taxYear.startDate.toString, taxYear.endDate.toString)
            ))
      }
    }

    "a valid request is supplied with the earliest possible taxYear" must {
      "return the parsed domain object" in {
        validator(validNino, Some("2017-18")).validateAndWrapResult() shouldBe
          Right(RetrieveCrystallisationObligationsRequest(Nino(validNino), ObligationsTaxYear("2017-04-06", "2018-04-05")))
      }
    }

    def test(nino: String, taxYear: String, error: MtdError): Unit = {
      s"return ${error.code} error" when {
        s"validating with ($nino, $taxYear) is supplied" in {
          validator(nino, Some(taxYear)).validateAndWrapResult() shouldBe Left(ErrorWrapper(correlationId, error))
        }
      }
    }

    Seq(
      ("A12344A", validTaxYear, NinoFormatError),
      (validNino, "201-20", TaxYearFormatError),
      (validNino, "2016-17", RuleTaxYearNotSupportedError),
      (validNino, "2018-20", RuleTaxYearRangeExceededError)
    ).foreach(args => (test _).tupled(args))

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator("A12344A", Some("20178")).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
