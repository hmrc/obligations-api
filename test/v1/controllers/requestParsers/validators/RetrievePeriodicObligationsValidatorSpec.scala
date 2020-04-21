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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors._
import v1.models.request.retrievePeriodObligations.RetrievePeriodicObligationsRawData

class RetrievePeriodicObligationsValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validTypeOfBusiness = "self-employment"
  private val validIncomeSourceId = "XAIS123456789012"
  private val validFromDate = "2019-01-01"
  private val validToDate = "2019-12-30"
  private val validStatus = "Open"

  val validator = new RetrievePeriodicObligationsValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrievePeriodicObligationsRawData(
          validNino,
          Some(validTypeOfBusiness),
          Some(validIncomeSourceId),
          Some(validFromDate),
          Some(validToDate),
          Some(validStatus))) shouldBe Nil
      }
      "a valid request is supplied with no incomeSourceId" in {
        validator.validate(RetrievePeriodicObligationsRawData(
          validNino,
          Some(validTypeOfBusiness),
          None,
          Some(validFromDate),
          Some(validToDate),
          Some(validStatus))) shouldBe Nil
      }
      "a valid request is supplied with no incomeSourceId & typeOfBusiness" in {
        validator.validate(RetrievePeriodicObligationsRawData(
          validNino,
          None,
          None,
          Some(validFromDate),
          Some(validToDate),
          Some(validStatus))) shouldBe Nil
      }
      "a valid request is supplied with no status" in {
        validator.validate(RetrievePeriodicObligationsRawData(
          validNino,
          Some(validTypeOfBusiness),
          Some(validIncomeSourceId),
          Some(validFromDate),
          Some(validToDate),
          None)) shouldBe Nil
      }
      "a valid request is supplied with none of the optional fields" in {
        validator.validate(RetrievePeriodicObligationsRawData(validNino, None, None, None, None, None)) shouldBe Nil
      }
    }

    "return a missing fromDate error" when {
      "the fromDate is missing while toDate exists" in {
        validator.validate(RetrievePeriodicObligationsRawData(
          validNino,
          Some(validTypeOfBusiness),
          Some(validIncomeSourceId),
          None,
          Some(validToDate),
          Some(validStatus))) shouldBe List(MissingFromDateError)
      }
    }
    "return a missing toDate error" when {
      "the toDate is missing while fromDate exists" in {
        validator.validate(RetrievePeriodicObligationsRawData(
          validNino,
          Some(validTypeOfBusiness),
          Some(validIncomeSourceId),
          Some(validFromDate),
          None,
          Some(validStatus))) shouldBe List(MissingToDateError)
      }
    }

    def test(nino: String,
             typeOfBusiness: String,
             incomeSourceId: String,
             fromDate: String,
             toDate: String,
             status: String,
             error: MtdError): Unit = {
             s"return ${error.code} error" when {
               s"RetrievePeriodicObligationsRawData($nino, $typeOfBusiness, $incomeSourceId, $fromDate, $toDate, $status) is supplied" in {
                 validator.validate(RetrievePeriodicObligationsRawData(
                   nino,
                   Some(typeOfBusiness),
                   Some(incomeSourceId),
                   Some(fromDate),
                   Some(toDate),
                   Some(status))) shouldBe List(error)
               }
             }
    }
    Seq(
      ("A23131", validTypeOfBusiness, validIncomeSourceId, validFromDate, validToDate, validStatus, NinoFormatError),
      (validNino, "Walrus", validIncomeSourceId, validFromDate, validToDate, validStatus, TypeOfBusinessFormatError),
      (validNino, validTypeOfBusiness, "Walrus", validFromDate, validToDate, validStatus, BusinessIdFormatError),
      (validNino, validTypeOfBusiness, validIncomeSourceId, "01-02-2019", validToDate, validStatus, FromDateFormatError),
      (validNino, validTypeOfBusiness, validIncomeSourceId, validToDate, "01-02-2019", validStatus, ToDateFormatError),
      (validNino, validTypeOfBusiness, validIncomeSourceId, "2017-01-01", "2018-01-01", validStatus, RuleFromDateNotSupportedError),
      (validNino, validTypeOfBusiness, validIncomeSourceId, "2019-01-01", "2018-01-01", validStatus, ToDateBeforeFromDateError),
      (validNino, validTypeOfBusiness, validIncomeSourceId, "2020-01-01", "2020-01-01", validStatus, RuleDateRangeInvalidError),
      (validNino, validTypeOfBusiness, validIncomeSourceId, "2018-12-12", "2020-04-05", validStatus, RuleDateRangeInvalidError),
      (validNino, validTypeOfBusiness, validIncomeSourceId, validFromDate, validToDate, "Walrus", StatusFormatError),
    ).foreach(args => (test _).tupled(args))

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator.validate(RetrievePeriodicObligationsRawData("Walrus", Some("Body"), Some("One"), Some("Thing"), Some("Where"), Some("Times"))) shouldBe
        List(NinoFormatError, BusinessIdFormatError, FromDateFormatError, ToDateFormatError, StatusFormatError, TypeOfBusinessFormatError)
      }
    }
  }

}
