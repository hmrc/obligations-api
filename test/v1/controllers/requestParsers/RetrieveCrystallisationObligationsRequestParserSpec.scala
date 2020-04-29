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

package v1.controllers.requestParsers

import java.time.LocalDate

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockRetrieveCrystallisationObligationsValidator
import v1.models.errors._
import v1.models.request.retrieveCrystallisationObligations.{RetrieveCrystallisationObligationsRawData, RetrieveCrystallisationObligationsRequest}
import v1.models.request.{ObligationsTaxYear, ObligationsTaxYearHelpers}

class RetrieveCrystallisationObligationsRequestParserSpec extends UnitSpec {
  val nino = "AA123456B"
  val overriddenDate: LocalDate = LocalDate.parse("2019-04-06")

  val inputData =
    RetrieveCrystallisationObligationsRawData(nino, Some("2018-19"))

  trait Test extends MockRetrieveCrystallisationObligationsValidator {
    val testDate: LocalDate
    lazy val parser = new RetrieveCrystallisationObligationsRequestParser(mockValidator) with ObligationsTaxYearHelpers {
      override val date: LocalDate = testDate
    }
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        override val testDate: LocalDate = overriddenDate

        MockRetrieveCrystallisationObligationsValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(RetrieveCrystallisationObligationsRequest(Nino(nino), ObligationsTaxYear("2018-04-06", "2019-04-05")))
      }
      "valid request data is supplied with no taxYear" in new Test {
        override val testDate: LocalDate = overriddenDate

        MockRetrieveCrystallisationObligationsValidator.validate(inputData.copy(taxYear = None)).returns(Nil)

        parser.parseRequest(inputData.copy(taxYear = None)) shouldBe
          Right(RetrieveCrystallisationObligationsRequest(Nino(nino), ObligationsTaxYear("2018-04-06", "2019-04-05")))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        override val testDate: LocalDate = overriddenDate

        MockRetrieveCrystallisationObligationsValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        override val testDate: LocalDate = overriddenDate

        MockRetrieveCrystallisationObligationsValidator.validate(inputData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }
}
