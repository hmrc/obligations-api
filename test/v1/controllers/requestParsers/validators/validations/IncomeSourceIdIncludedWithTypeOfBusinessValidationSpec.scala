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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.MissingTypeOfBusinessError
import v1.models.utils.JsonErrorValidators

class IncomeSourceIdIncludedWithTypeOfBusinessValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    val businessId = Some("XAIS123456789012")
    val typeOfBusiness = Some("F")

    "return no errors" when {
      "when a businessId and typeOfBusiness is supplied" in {
        val validationResult = IncomeSourceIdIncludedWithTypeOfBusinessValidation.validate(businessId, typeOfBusiness)
        validationResult.isEmpty shouldBe true
      }
      "when neither a businessId or typeOfBusiness is supplied" in {
        val validationResult = IncomeSourceIdIncludedWithTypeOfBusinessValidation.validate(None, None)
        validationResult.isEmpty shouldBe true
      }
      "when a businessId is supplied but not a typeOfBusiness" in {
        val validationResult = IncomeSourceIdIncludedWithTypeOfBusinessValidation.validate(businessId, None)
        validationResult.isEmpty shouldBe true
      }
    }
    "return a missing business Id Error" when {
      "when the typeOfBusiness is supplied but not a businessId" in {
        val validationResult = IncomeSourceIdIncludedWithTypeOfBusinessValidation.validate(None, typeOfBusiness)
        validationResult.isEmpty shouldBe false
        validationResult.head shouldBe MissingTypeOfBusinessError
      }
    }
  }

}
