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
import api.models.errors.*
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.Inside
import support.UnitSpec

import java.time.LocalDate

class ResolveOptionalDateRangeSpec extends UnitSpec with Inside {

  "resolver" when {
    "the from date is incorrectly formatted" must {
      "return with FromDateFormatError" in {
        ResolveOptionalDateRange.resolver((Some("badDate"), Some("2020-01-01"))) shouldBe Invalid(List(FromDateFormatError))
      }
    }

    "the to date is incorrectly formatted" must {
      "return with ToDateFormatError" in {
        ResolveOptionalDateRange.resolver((Some("2020-01-01"), Some("badDate"))) shouldBe Invalid(List(ToDateFormatError))
      }
    }

    "both dates are incorrectly formatted" must {
      "return with FromDateFormatError and ToDateFormatError" in {
        inside(ResolveOptionalDateRange.resolver((Some("badDate"), Some("badDate")))) { case Invalid(errs) =>
          errs should contain allElementsOf List(FromDateFormatError, ToDateFormatError)
        }
      }
    }

    "only the from date is supplied" must {
      "return with MissingToDateError" in {
        ResolveOptionalDateRange.resolver((Some("2020-01-01"), None)) shouldBe Invalid(List(MissingToDateError))
      }
    }

    "only the to date is supplied" must {
      "return with MissingFromDateError" in {
        ResolveOptionalDateRange.resolver((None, Some("2020-01-01"))) shouldBe Invalid(List(MissingFromDateError))
      }
    }

    "both dates are supplied and formatted correctly" must {
      "return the DateRange" in {
        val from = "2020-01-01"
        val to   = "2020-01-02"
        ResolveOptionalDateRange.resolver((Some(from), Some(to))) shouldBe
          Valid(Some(DateRange(LocalDate.parse(from), LocalDate.parse(to))))
      }
    }

    "neither date is supplied" must {
      "return None" in {
        ResolveOptionalDateRange.resolver((None, None)) shouldBe Valid(None)
      }
    }
  }

}
