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

import api.models.domain.Nino
import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatus
import api.models.errors.{ BadRequestError, ErrorWrapper, NinoFormatError, TypeOfBusinessFormatError }
import support.UnitSpec
import v1.mocks.validators.MockRetrievePeriodicObligationsValidator
import v1.models.request.retrievePeriodObligations.{ RetrievePeriodicObligationsRawData, RetrievePeriodicObligationsRequest }

import java.time.LocalDate

class RetrievePeriodicObligationsRequestParserSpec extends UnitSpec {
  val nino                                 = "AA123456B"
  val typeOfBusiness                       = "self-employment"
  val convertedTypeOfBusiness: MtdBusiness = MtdBusiness.`self-employment`
  val businessId                           = "XAIS123456789012"
  val fromDate                             = "2019-01-01"
  val toDate                               = "2020-01-01"
  val status                               = "Open"
  val convertedStatus: MtdStatus           = MtdStatus.Open
  val convertedStatusFulfilled: MtdStatus  = MtdStatus.Fulfilled
  implicit val correlationId: String       = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val data: RetrievePeriodicObligationsRawData =
    RetrievePeriodicObligationsRawData(nino, Some(typeOfBusiness), Some(businessId), Some(fromDate), Some(toDate), Some(status))

  val todaysDatesData: RetrievePeriodicObligationsRawData =
    RetrievePeriodicObligationsRawData(nino, Some(typeOfBusiness), Some(businessId), None, None, Some("Fulfilled"))

  val invalidNinoData: RetrievePeriodicObligationsRawData =
    RetrievePeriodicObligationsRawData("Walrus", Some(typeOfBusiness), Some(businessId), Some(fromDate), Some(toDate), Some(status))

  val invalidMultipleData: RetrievePeriodicObligationsRawData =
    RetrievePeriodicObligationsRawData("Walrus", Some("Beans"), Some(businessId), Some(fromDate), Some(toDate), Some(status))
  val todaysDate: String    = LocalDate.now().toString
  val nextYearsDate: String = LocalDate.now().plusDays(366).toString

  trait Test extends MockRetrievePeriodicObligationsValidator {
    lazy val parser = new RetrievePeriodicObligationsRequestParser(mockValidator)
  }

  "parse" should {
    "return a RetrieveRequest" when {
      "the validator returns no errors" in new Test {
        MockRetrievePeriodicObligationsValidator.validate(data).returns(Nil)
        parser.parseRequest(data) shouldBe Right(
          RetrievePeriodicObligationsRequest(Nino(nino),
                                             Some(convertedTypeOfBusiness),
                                             Some(businessId),
                                             Some(fromDate),
                                             Some(toDate),
                                             Some(convertedStatus)))
      }
    }
    "return an error wrapper" when {
      "the validator returns a single error" in new Test {
        MockRetrievePeriodicObligationsValidator.validate(invalidNinoData).returns(List(NinoFormatError))
        parser.parseRequest(invalidNinoData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
      "the validator returns multiple errors" in new Test {
        MockRetrievePeriodicObligationsValidator.validate(invalidMultipleData).returns(List(NinoFormatError, TypeOfBusinessFormatError))
        parser.parseRequest(invalidMultipleData) shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TypeOfBusinessFormatError))))
      }
    }
    "convert fromDate to today and toDate to 366 days ahead" when {
      "there are no dates input and the status is Fulfilled" in new Test {
        MockRetrievePeriodicObligationsValidator.validate(todaysDatesData).returns(Nil)
        parser.parseRequest(todaysDatesData) shouldBe {
          Right(
            RetrievePeriodicObligationsRequest(Nino(nino),
                                               Some(convertedTypeOfBusiness),
                                               Some(businessId),
                                               Some(todaysDate),
                                               Some(nextYearsDate),
                                               Some(convertedStatusFulfilled)))
        }
      }
      "there are no dates input and the status is empty" in new Test {
        MockRetrievePeriodicObligationsValidator.validate(todaysDatesData.copy(status = None)).returns(Nil)
        parser.parseRequest(todaysDatesData.copy(status = None)) shouldBe {
          Right(
            RetrievePeriodicObligationsRequest(Nino(nino),
                                               Some(convertedTypeOfBusiness),
                                               Some(businessId),
                                               Some(todaysDate),
                                               Some(nextYearsDate),
                                               None))
        }
      }
    }
  }

}
