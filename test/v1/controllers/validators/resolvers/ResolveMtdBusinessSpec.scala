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

package v1.controllers.validators.resolvers

import api.models.domain.business.MtdBusiness
import api.models.errors.TypeOfBusinessFormatError
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveMtdBusinessSpec extends UnitSpec {

  "ResolveMtdBusiness" should {

    "resolve valid values" in {
      ResolveMtdBusiness.resolver("self-employment") shouldBe Valid(MtdBusiness.`self-employment`)
      ResolveMtdBusiness.resolver("uk-property") shouldBe Valid(MtdBusiness.`uk-property`)
      ResolveMtdBusiness.resolver("foreign-property") shouldBe Valid(MtdBusiness.`foreign-property`)
    }

    "not resolve the invalid values" in {
      ResolveMtdBusiness.resolver("notABusinessType") shouldBe Invalid(Seq(TypeOfBusinessFormatError))
    }
  }

}
