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

import api.models.domain.TransactionId
import api.models.errors.TransactionIdFormatError
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveTransactionIdSpec extends UnitSpec {

  "ResolveTransactionId" should {
    "return no errors" when {
      "given a valid Transaction ID" in {
        val value  = "1234567890AB"
        val result = ResolveTransactionId(value)
        result shouldBe Valid(TransactionId(value))
      }
    }

    "return an error" when {
      "given an invalid TransactionId" in {
        val result = ResolveTransactionId("not-a-transaction-id")
        result shouldBe Invalid(List(TransactionIdFormatError))
      }
    }
  }

}