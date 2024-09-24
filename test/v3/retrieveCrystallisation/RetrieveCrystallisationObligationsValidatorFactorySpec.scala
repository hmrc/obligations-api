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

package v3.retrieveCrystallisation

import api.models.domain.status.MtdStatusV3
import api.models.domain.{Nino, TaxYear, TaxYearRange}
import api.models.errors._
import org.threeten.extra.MutableClock
import support.UnitSpec
import v3.retrieveCrystallisation.RetrieveCrystallisationObligationsValidatorFactory
import v3.retrieveCrystallisation.model.request.RetrieveCrystallisationObligationsRequest

import java.time.Instant

class RetrieveCrystallisationObligationsValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"
  private implicit val clock: MutableClock   = MutableClock.epochUTC()

  val validatorFactory  = new RetrieveCrystallisationObligationsValidatorFactory
  private val validNino = "AA123456A"

  private def validator(nino: String, taxYear: Option[String], status: Option[String]) =
    validatorFactory.validator(nino, taxYear, status)

  "validator" when {
    "a valid request is supplied" must {
      "return the parsed domain object" in {
        validator(validNino, Some("2018-19"), Some("open")).validateAndWrapResult() shouldBe
          Right(RetrieveCrystallisationObligationsRequest(Nino(validNino), TaxYearRange.fromMtd("2018-19"), Some(MtdStatusV3.open)))
      }
    }

    "a valid request is supplied with no optional values" must {
      "return the parsed domain object for the last 4 years" in {
        clock.setInstant(Instant.parse("2020-01-01T12:34:56.789Z"))

        val endTaxYear   = TaxYear.ending(2020)
        val startTaxYear = TaxYear.ending(2016)

        validator(validNino, None, None).validateAndWrapResult() shouldBe
          Right(
            RetrieveCrystallisationObligationsRequest(
              Nino(validNino),
              TaxYearRange(startTaxYear, endTaxYear),
              None
            ))
      }
    }

    "a valid request is supplied with the earliest possible taxYear" must {
      "return the parsed domain object" in {
        validator(validNino, Some("2017-18"), None).validateAndWrapResult() shouldBe
          Right(RetrieveCrystallisationObligationsRequest(Nino(validNino), TaxYearRange.fromMtd("2017-18"), None))
      }
    }

    def test(nino: String, taxYear: String, status: String, error: MtdError): Unit = {
      s"return ${error.code} error" when {
        s"validating with ($nino, $taxYear) is supplied" in {
          validator(nino, Some(taxYear), Some(status)).validateAndWrapResult() shouldBe Left(ErrorWrapper(correlationId, error))
        }
      }
    }

    val validTaxYear = "2018-19"
    val validStatus  = "open"

    Seq(
      ("A12344A", validTaxYear, validStatus, NinoFormatError),
      (validNino, "201-20", validStatus, TaxYearFormatError),
      (validNino, "2016-17", validStatus, RuleTaxYearNotSupportedError),
      (validNino, "2018-20", validStatus, RuleTaxYearRangeInvalidError),
      (validNino, validTaxYear, "notAStatus", StatusFormatError)
    ).foreach(args => (test _).tupled(args))

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator("A12344A", Some("20178"), None).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
