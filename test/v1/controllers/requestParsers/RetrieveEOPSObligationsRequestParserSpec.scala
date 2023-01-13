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

package v1.controllers.requestParsers

import java.time.LocalDate

import support.UnitSpec
import v1.models.domain.Nino
import v1.mocks.validators.MockRetrieveEOPSObligationsValidator
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.MtdStatus
import v1.models.errors.{ BadRequestError, ErrorWrapper, NinoFormatError, TypeOfBusinessFormatError }
import v1.models.request.retrieveEOPSObligations.{ RetrieveEOPSObligationsRawData, RetrieveEOPSObligationsRequest }

class RetrieveEOPSObligationsRequestParserSpec extends UnitSpec {

  val nino                     = "AA123456B"
  val typeOfBusiness           = "self-employment"
  val convertedTypeOfBusiness  = MtdBusiness.`self-employment`
  val businessId               = "XAIS123456789012"
  val fromDate                 = "2019-01-01"
  val toDate                   = "2020-01-01"
  val status                   = "Open"
  val convertedStatusOpen      = MtdStatus.Open
  val convertedStatusFulfilled = MtdStatus.Fulfilled
  val data                     = RetrieveEOPSObligationsRawData(nino, Some(typeOfBusiness), Some(businessId), Some(fromDate), Some(toDate), Some(status))
  val todaysDatesData          = RetrieveEOPSObligationsRawData(nino, Some(typeOfBusiness), Some(businessId), None, None, Some("Fulfilled"))
  val invalidNinoData          = RetrieveEOPSObligationsRawData("Walrus", Some(typeOfBusiness), Some(businessId), Some(fromDate), Some(toDate), Some(status))
  val invalidMultipleData      = RetrieveEOPSObligationsRawData("Walrus", Some("Beans"), Some(businessId), Some(fromDate), Some(toDate), Some(status))
  val todaysDate               = LocalDate.now().toString
  val nextYearsDate            = LocalDate.now().plusDays(366).toString

  trait Test extends MockRetrieveEOPSObligationsValidator {
    lazy val parser = new RetrieveEOPSObligationsRequestParser(mockValidator)
  }

  "parse" should {
    "return a RetrieveRequest" when {
      "the validator returns no errors" in new Test {
        MockRetrieveEOPSObligationsValidator.validate(data).returns(Nil)
        parser.parseRequest(data) shouldBe Right(
          RetrieveEOPSObligationsRequest(Nino(nino),
                                         Some(convertedTypeOfBusiness),
                                         Some(businessId),
                                         Some(fromDate),
                                         Some(toDate),
                                         Some(convertedStatusOpen)))
      }
    }
    "return an error wrapper" when {
      "the validator returns a single error" in new Test {
        MockRetrieveEOPSObligationsValidator.validate(invalidNinoData).returns(List(NinoFormatError))
        parser.parseRequest(invalidNinoData) shouldBe Left(ErrorWrapper(None, NinoFormatError, None))
      }
      "the validator returns multiple errors" in new Test {
        MockRetrieveEOPSObligationsValidator.validate(invalidMultipleData).returns(List(NinoFormatError, TypeOfBusinessFormatError))
        parser.parseRequest(invalidMultipleData) shouldBe Left(
          ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TypeOfBusinessFormatError))))
      }
    }
    "convert fromDate to today and toDate to 366 days ahead" when {
      "there are no dates input and the status is Fulfilled" in new Test {
        MockRetrieveEOPSObligationsValidator.validate(todaysDatesData).returns(Nil)
        parser.parseRequest(todaysDatesData) shouldBe Right(
          RetrieveEOPSObligationsRequest(Nino(nino),
                                         Some(convertedTypeOfBusiness),
                                         Some(businessId),
                                         Some(todaysDate),
                                         Some(nextYearsDate),
                                         Some(convertedStatusFulfilled)))
      }
      "there are no dates input and the status is empty" in new Test {
        MockRetrieveEOPSObligationsValidator.validate(todaysDatesData.copy(status = None)).returns(Nil)
        parser.parseRequest(todaysDatesData.copy(status = None)) shouldBe Right(
          RetrieveEOPSObligationsRequest(Nino(nino), Some(convertedTypeOfBusiness), Some(businessId), Some(todaysDate), Some(nextYearsDate), None))
      }
    }
  }
}
