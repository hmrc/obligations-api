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

package v2.controllers.validators.resolvers

import api.models.domain.DateRange
import api.models.errors.{RuleDateRangeInvalidError, RuleFromDateNotSupportedError, ToDateBeforeFromDateError, ToDateFormatError}
import org.scalatest.Inside
import support.UnitSpec

import java.time.LocalDate

class ObligationsDateRangeSupportSpec extends UnitSpec with Inside {

  private def dateRangeFrom(from: String, to: String): DateRange =
    DateRange(LocalDate.parse(from), LocalDate.parse(to))

  "validate" when {
    "the date range is valid" must {
      "return Valid" in {
        val dateRange = dateRangeFrom("2019-01-01", "2020-01-01")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe None
      }
    }

    "the to date is before the from date" must {
      "return with ToDateBeforeFromDateError" in {
        val dateRange = dateRangeFrom("2019-01-02", "2019-01-01")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe Some(Seq(ToDateBeforeFromDateError))
      }
    }

    "the number of days spanned is a single day" must {
      "return with RuleDateRangeInvalidError" in {
        val dateRange = dateRangeFrom("2020-01-01", "2020-01-01")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe Some(Seq(RuleDateRangeInvalidError))
      }
    }

    "the number of days spanned is two days" must {
      "return Valid" in {
        val dateRange = dateRangeFrom("2020-01-01", "2020-01-02")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe None
      }
    }

    "the from date is before the minimum" must {
      "return with RuleFromDateNotSupportedError" in {
        val dateRange = dateRangeFrom("2018-04-05", "2019-01-01")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe Some(Seq(RuleFromDateNotSupportedError))
      }
    }

    "the from date is equal to the minimum" must {
      "return Valid" in {
        val dateRange = dateRangeFrom("2018-04-06", "2019-01-01")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe None
      }
    }

    "the from date is after the maximum" must {
      // NOTE :This is extra validation added to prevent out of range dates so it re-uses an existing error
      "return with ToDateFormatError" in {
        val dateRange = dateRangeFrom("2099-12-01", "2100-01-01")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe Some(Seq(ToDateFormatError))
      }
    }

    "the from date is equal to the maximum" must {
      "return Valid" in {
        val dateRange = dateRangeFrom("2099-12-01", "2099-12-31")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe None
      }
    }

    "the number of days spanned is too large" must {
      "return with RuleDateRangeInvalidError" in {
        // 2020 has 366 days so this inclusive range has more...
        val dateRange = dateRangeFrom("2020-01-01", "2021-01-01")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe Some(Seq(RuleDateRangeInvalidError))
      }
    }

    "the number of days spanned is at maximum" must {
      "return Valid" in {
        // 2020 has 366 days so this is ok...
        val dateRange = dateRangeFrom("2020-01-01", "2020-12-31")
        ObligationsDateRangeSupport.validator(dateRange) shouldBe None
      }
    }

    "multiple errors are detected" must {
      "return all errors found" in {
        val dateRange = dateRangeFrom("2000-01-01", "1999-01-01")
        inside(ObligationsDateRangeSupport.validator(dateRange)) { case Some(errs) =>
          errs should contain theSameElementsAs List(RuleFromDateNotSupportedError, ToDateBeforeFromDateError)
        }
      }
    }
  }

}
