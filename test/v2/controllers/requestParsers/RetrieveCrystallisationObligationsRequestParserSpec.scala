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

package v2.controllers.requestParsers

import api.models.domain.status.MtdStatus
import api.models.domain.{ Nino, TaxYear }
import api.models.errors._
import api.models.request.TaxYearRange
import support.UnitSpec
import v2.mocks.validators.MockRetrieveCrystallisationObligationsValidator
import v2.models.request.retrieveCrystallisationObligations.{ RetrieveCrystallisationObligationsRawData, RetrieveCrystallisationObligationsRequest }

class RetrieveCrystallisationObligationsRequestParserSpec extends UnitSpec {
  val nino = "AA123456B"

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val inputData: RetrieveCrystallisationObligationsRawData =
    RetrieveCrystallisationObligationsRawData(nino, Some("2018-19"), Some("Open"))

  trait Test extends MockRetrieveCrystallisationObligationsValidator {

    val defaultRange: TaxYearRange =
      TaxYearRange(TaxYear.fromMtd("2018-19"), TaxYear.fromMtd("2023-24"))

    lazy val parser: RetrieveCrystallisationObligationsRequestParser =
      new RetrieveCrystallisationObligationsRequestParser(mockValidator) {
        override protected def defaultTaxYearRange(): TaxYearRange = defaultRange
      }
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveCrystallisationObligationsValidator.validate(inputData).returns(Nil)

        val result: Either[ErrorWrapper, RetrieveCrystallisationObligationsRequest] = parser.parseRequest(inputData)
        result shouldBe
          Right(
            RetrieveCrystallisationObligationsRequest(
              Nino(nino),
              TaxYearRange.fromMtd("2018-19"),
              Some(MtdStatus.Open)
            ))
      }

      "valid request data is supplied with no taxYear or status params" in new Test {
        private val rawData = inputData.copy(taxYear = None, status = None)

        MockRetrieveCrystallisationObligationsValidator.validate(rawData).returns(Nil)

        val result: Either[ErrorWrapper, RetrieveCrystallisationObligationsRequest] =
          parser.parseRequest(rawData)
        result shouldBe
          Right(
            RetrieveCrystallisationObligationsRequest(
              Nino(nino),
              defaultRange,
              status = None
            )
          )
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockRetrieveCrystallisationObligationsValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        val result: Either[ErrorWrapper, RetrieveCrystallisationObligationsRequest] = parser.parseRequest(inputData)
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockRetrieveCrystallisationObligationsValidator
          .validate(inputData)
          .returns(List(NinoFormatError, TaxYearFormatError, StatusFormatError))

        val result: Either[ErrorWrapper, RetrieveCrystallisationObligationsRequest] = parser.parseRequest(inputData)
        result shouldBe Left(
          ErrorWrapper(correlationId,
                       BadRequestError,
                       Some(
                         List(
                           NinoFormatError,
                           TaxYearFormatError,
                           StatusFormatError
                         ))))
      }
    }
  }
}
