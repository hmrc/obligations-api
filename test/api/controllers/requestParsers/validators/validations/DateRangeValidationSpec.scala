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

package api.controllers.requestParsers.validators.validations

import api.models.errors.{RuleDateRangeInvalidError, RuleFromDateNotSupportedError, ToDateBeforeFromDateError, ToDateFormatError}
import support.UnitSpec

class DateRangeValidationSpec extends UnitSpec {

  "validate" should {
    "return no errors" when {
      "a valid date range is supplied" in {
        DateRangeValidation.validate("2019-01-01", "2020-01-01") shouldBe Nil
      }
    }
    "return a Date range invalid rule error" when {
      "the 'To Date' is before the 'From Date'" in {
        DateRangeValidation.validate("2020-01-01", "2019-01-01") shouldBe List(ToDateBeforeFromDateError)
      }
      "the 'From Date' is before the earliest allowed date" in {
        DateRangeValidation.validate("2015-01-01", "2016-01-01") shouldBe List(RuleFromDateNotSupportedError)
      }
      "the 'To Date' is after the latest allowed date" in {
        DateRangeValidation.validate(from = "2099-12-01", to = "2100-05-01") shouldBe List(ToDateFormatError)
      }
      "the date range is too large" in {
        DateRangeValidation.validate("2018-11-01", "2020-01-01") shouldBe List(RuleDateRangeInvalidError)
      }
      "the date range is too short" in {
        DateRangeValidation.validate("2020-01-01", "2020-01-01") shouldBe List(RuleDateRangeInvalidError)
      }
    }
  }

}
