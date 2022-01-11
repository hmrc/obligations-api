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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.FromDateFormatError

class DateValidationSpec extends UnitSpec {

  "validate" should {
    "return no errors" when {
      "when a valid date is supplied" in {
        val validationResult = DateValidation.validate("2019-01-01", FromDateFormatError)
        validationResult shouldBe Nil
      }
      "when an empty string is supplied" in {
        val validationResult = DateValidation.validate("", FromDateFormatError)
        validationResult shouldBe Nil
      }
    }
    "return a format error" when {
      "the date format is wrong" in {
        val validationResult = DateValidation.validate("01-02-2019", FromDateFormatError)
        validationResult.nonEmpty shouldBe true
        validationResult.head shouldBe FromDateFormatError
      }
    }
  }

}
