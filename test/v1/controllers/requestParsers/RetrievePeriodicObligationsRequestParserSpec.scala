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

package v1.controllers.requestParsers

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.RetrieveMockPeriodicObligationsValidator
import v1.models.domain.business.DesBusiness
import v1.models.domain.status.DesStatus
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TypeOfBusinessFormatError}
import v1.models.request.retrievePeriodObligations.{RetrievePeriodicObligationsRawData, RetrievePeriodicObligationsRequest}

class RetrievePeriodicObligationsRequestParserSpec extends UnitSpec {
  val nino = "AA123456B"
  val typeOfBusiness = "ITSP"
  val convertedTypeOfBusiness = DesBusiness.ITSP
  val incomeSourceId = "XAIS123456789012"
  val fromDate = "2019-01-01"
  val toDate = "2020-01-01"
  val status = "O"
  val convertedStatus = DesStatus.O
  val data = RetrievePeriodicObligationsRawData(nino, Some(typeOfBusiness), Some(incomeSourceId), Some(fromDate), Some(toDate), Some(status))
  val invalidNinoData = RetrievePeriodicObligationsRawData("Walrus", Some(typeOfBusiness), Some(incomeSourceId), Some(fromDate), Some(toDate), Some(status))
  val invalidMultipleData = RetrievePeriodicObligationsRawData("Walrus", Some("Beans"), Some(incomeSourceId), Some(fromDate), Some(toDate), Some(status))

  trait Test extends RetrieveMockPeriodicObligationsValidator {
    lazy val parser = new RetrievePeriodicObligationsRequestParser(mockValidator)
  }

  "parse" should {
    "return a RetrieveRequest" when {
      "the validator returns no errors" in new Test {
        RetrieveMockPeriodicObligationsValidator.validate(data).returns(Nil)
        parser.parseRequest(data) shouldBe Right(RetrievePeriodicObligationsRequest(Nino(nino), Some(convertedTypeOfBusiness), Some(incomeSourceId), Some(fromDate), Some(toDate), Some(convertedStatus)))
      }
    }
    "return an error wrapper" when {
      "the validator returns a single error" in new Test {
        RetrieveMockPeriodicObligationsValidator.validate(invalidNinoData).returns(List(NinoFormatError))
        parser.parseRequest(invalidNinoData) shouldBe Left(ErrorWrapper(None, NinoFormatError, None))
      }
      "the validator returns multiple errors" in new Test {
        RetrieveMockPeriodicObligationsValidator.validate(invalidMultipleData).returns(List(NinoFormatError, TypeOfBusinessFormatError))
        parser.parseRequest(invalidMultipleData) shouldBe Left(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TypeOfBusinessFormatError))))
      }
    }
  }

}
