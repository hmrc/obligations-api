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

package api.models.errors

import api.models.audit.AuditError
import api.models.errors
import play.api.libs.json.Json
import support.UnitSpec

class ErrorWrapperSpec extends UnitSpec {

  val correlationId = "X-123"

  "Rendering a error response with one error" should {
    val error = ErrorWrapper(correlationId, NinoFormatError, Some(Seq.empty))

    val json = Json.parse(
      """
        |{
        |   "code": "FORMAT_NINO",
        |   "message": "The provided NINO is invalid"
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }

  "Rendering a error response with one error and an empty sequence of errors" should {
    val error = errors.ErrorWrapper(correlationId, NinoFormatError, Some(Seq.empty))

    val json = Json.parse(
      """
        |{
        |   "code": "FORMAT_NINO",
        |   "message": "The provided NINO is invalid"
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }

  "Rendering a error response with two errors" should {
    val error = errors.ErrorWrapper(correlationId,
                                    BadRequestError,
                                    Some(
                                      Seq(
                                        NinoFormatError,
                                        TaxYearFormatError
                                      )
                                    ))

    val json = Json.parse(
      """
        |{
        |   "code": "INVALID_REQUEST",
        |   "message": "Invalid request",
        |   "errors": [
        |       {
        |         "code": "FORMAT_NINO",
        |         "message": "The provided NINO is invalid"
        |       },
        |       {
        |         "code": "FORMAT_TAX_YEAR",
        |         "message": "The provided tax year is invalid"
        |       }
        |   ]
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }

  "auditErrors" should {
    "handle errors = None" in {
      val errorWrapper = errors.ErrorWrapper(correlationId, BadRequestError, None)
      errorWrapper.auditErrors shouldBe Seq(AuditError(BadRequestError.code))
    }
    "handle errors = Some(_)" in {
      val errorWrapper = errors.ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError)))
      errorWrapper.auditErrors shouldBe Seq(AuditError(NinoFormatError.code), AuditError(TaxYearFormatError.code))
    }
  }

  "When ErrorWrapper has several errors, containsAnyOf" should {
    val errorWrapper =
      errors.ErrorWrapper("correlationId", BadRequestError, Some(List(NinoFormatError, TaxYearFormatError, ServiceUnavailableError)))

    "return false" when {
      "given no matching errors" in {
        val result = errorWrapper.containsAnyOf(TaxYearFormatError, ServiceUnavailableError)
        result shouldBe false
      }
      "given a matching error in 'errors' but not the single 'error' which should be a BadRequestError" in {
        val result = errorWrapper.containsAnyOf(NinoFormatError, TaxYearFormatError, ServiceUnavailableError)
        result shouldBe false
      }
    }
    "return true" when {
      "given the 'single' BadRequestError" in {
        val result = errorWrapper.containsAnyOf(NinoFormatError, BadRequestError, TaxYearFormatError, ServiceUnavailableError)
        result shouldBe true
      }
    }
  }

}
