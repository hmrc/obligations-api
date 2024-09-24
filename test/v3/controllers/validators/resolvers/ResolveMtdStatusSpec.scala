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

package v3.controllers.validators.resolvers

import api.models.domain.status.MtdStatusV3
import api.models.errors.StatusFormatError
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveMtdStatusSpec extends UnitSpec {

  "ResolveMtdStatus" should {

    "resolve valid values" in {
      ResolveMtdStatus.resolver("open") shouldBe Valid(MtdStatusV3.open)
      ResolveMtdStatus.resolver("fulfilled") shouldBe Valid(MtdStatusV3.fulfilled)
    }

    "not resolve the invalid values" in {
      ResolveMtdStatus.resolver("notAStatus") shouldBe Invalid(Seq(StatusFormatError))
    }
  }

}
