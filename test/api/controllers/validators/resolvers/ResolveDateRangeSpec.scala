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

package api.controllers.validators.resolvers

import api.models.domain.DateRange
import api.models.errors.{EndDateFormatError, RuleEndBeforeStartDateError, StartDateFormatError}
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

import java.time.LocalDate

class ResolveDateRangeSpec extends UnitSpec {

  private val validStart = "2023-06-21"
  private val validEnd   = "2024-06-21"

  // To be sure it's using the construction params...
  private val startDateFormatError    = StartDateFormatError.withPath("somePath")
  private val endDateFormatError      = EndDateFormatError.withPath("somePath")
  private val endBeforeStartDateError = RuleEndBeforeStartDateError.withPath("somePath")

  private val resolveDateRange = ResolveDateRange(
    startDateFormatError = startDateFormatError,
    endDateFormatError = endDateFormatError,
    endBeforeStartDateError = endBeforeStartDateError)

  "ResolveDateRange" should {
    "return no errors" when {
      "passed a valid start and end date" in {
        val result = resolveDateRange(validStart -> validEnd)
        result shouldBe Valid(DateRange(LocalDate.parse(validStart), LocalDate.parse(validEnd)))
      }
    }

    "return an error" when {
      "passed an invalid start date" in {
        val result = resolveDateRange("not-a-date" -> validEnd)
        result shouldBe Invalid(List(startDateFormatError))
      }

      "passed an invalid end date" in {
        val result = resolveDateRange(validStart -> "not-a-date")
        result shouldBe Invalid(List(endDateFormatError))
      }

      "passed an end date before start date" in {
        val result = resolveDateRange(validEnd -> validStart)
        result shouldBe Invalid(List(endBeforeStartDateError))
      }
    }
  }

  "ResolveDateRange datesLimitedTo validator" must {
    val minDate   = LocalDate.parse("2000-02-01")
    val maxDate   = LocalDate.parse("2000-02-10")
    val validator = ResolveDateRange.datesLimitedTo(minDate, startDateFormatError, maxDate, endDateFormatError)

    "allow min and max dates" in {
      validator(DateRange(minDate, maxDate)) shouldBe None
    }

    "disallow dates earlier than min or later than max" in {
      validator(DateRange(minDate.minusDays(1), maxDate.plusDays(1))) shouldBe Some(List(startDateFormatError, endDateFormatError))
    }
  }

  "ResolveDateRange yearsLimitedTo validator" must {
    val minYear = 2000
    val maxYear = 2010

    val validator = ResolveDateRange.yearsLimitedTo(minYear, startDateFormatError, maxYear, endDateFormatError)

    "allow dates in min and max years" in {
      validator(DateRange(LocalDate.parse("2000-01-01"), LocalDate.parse("2010-12-31"))) shouldBe None
    }

    "disallow dates earlier than min year or later than max year" in {
      validator(DateRange(LocalDate.parse("1999-12-31"), LocalDate.parse("2011-01-01"))) shouldBe Some(List(startDateFormatError, endDateFormatError))
    }
  }

}
