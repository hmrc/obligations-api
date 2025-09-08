/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.retrievePeriodic

import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatusV3
import api.models.domain.{BusinessId, DateRange, Nino}
import api.models.errors.*
import org.scalatest.Inside
import org.threeten.extra.MutableClock
import support.UnitSpec
import v3.retrievePeriodic.model.request.RetrievePeriodicObligationsRequest

import java.time.{Instant, LocalDate}

class RetrievePeriodicObligationsValidatorFactorySpec extends UnitSpec with Inside {

  private implicit val correlationId: String = "1234"
  private implicit val clock: MutableClock   = MutableClock.epochUTC()

  val validatorFactory = new RetrievePeriodicObligationsValidatorFactory

  private val validNino           = "AA123456A"
  private val validTypeOfBusiness = "self-employment"
  private val validBusinessId     = "XAIS12345678901"
  private val validFromDate       = "2019-01-01"
  private val validToDate         = "2019-12-30"
  private val validStatus         = "open"

  "running a validation" when {
    "all fields are supplied" must {
      "return the parsed domain object" in {
        validatorFactory
          .validator(
            nino = validNino,
            typeOfBusiness = Some(validTypeOfBusiness),
            businessId = Some(validBusinessId),
            fromDate = Some(validFromDate),
            toDate = Some(validToDate),
            status = Some(validStatus)
          )
          .validateAndWrapResult() shouldBe
          Right(
            RetrievePeriodicObligationsRequest(
              nino = Nino(validNino),
              typeOfBusiness = Some(MtdBusiness.`self-employment`),
              businessId = Some(BusinessId(validBusinessId)),
              dateRange = Some(DateRange(LocalDate.parse(validFromDate), LocalDate.parse(validToDate))),
              status = Some(MtdStatusV3.open)
            ))
      }
    }

    "only an Open status and no other fields are supplied" must {
      "return the parsed domain object with None in the other fields" in {
        validatorFactory
          .validator(validNino, None, None, None, None, Some("open"))
          .validateAndWrapResult() shouldBe
          Right(RetrievePeriodicObligationsRequest(Nino(validNino), None, None, None, Some(MtdStatusV3.open)))
      }
    }

    "only a Fulfilled status and no other fields are supplied" must {
      "return the parsed domain object with for a year (now to now + 366 days)" in {
        clock.setInstant(Instant.parse("2020-01-01T12:34:56.789Z"))

        validatorFactory
          .validator(validNino, None, None, None, None, Some("fulfilled"))
          .validateAndWrapResult() shouldBe
          Right(
            RetrievePeriodicObligationsRequest(
              nino = Nino(validNino),
              typeOfBusiness = None,
              businessId = None,
              dateRange = Some(DateRange(LocalDate.parse("2020-01-01"), LocalDate.parse("2021-01-01"))),
              status = Some(MtdStatusV3.fulfilled)
            ))
      }
    }

    "no fields are supplied" must {
      "return the parsed domain object with for a year (now to now + 366 days)" in {
        clock.setInstant(Instant.parse("2020-01-01T12:34:56.789Z"))

        validatorFactory
          .validator(validNino, None, None, None, None, None)
          .validateAndWrapResult() shouldBe
          Right(
            RetrievePeriodicObligationsRequest(
              nino = Nino(validNino),
              typeOfBusiness = None,
              businessId = None,
              dateRange = Some(DateRange(LocalDate.parse("2020-01-01"), LocalDate.parse("2021-01-01"))),
              status = None
            ))
      }
    }

    "the fromDate is missing while toDate is provided" must {
      "return a MissingFromDateError" in {
        validatorFactory
          .validator(validNino, Some(validTypeOfBusiness), Some(validBusinessId), None, Some(validToDate), Some(validStatus))
          .validateAndWrapResult() shouldBe Left(ErrorWrapper(correlationId, MissingFromDateError))
      }
    }

    "the toDate is missing while fromDate is provided" must {
      "return a MissingToDateError" in {
        validatorFactory
          .validator(validNino, Some(validTypeOfBusiness), Some(validBusinessId), Some(validFromDate), None, Some(validStatus))
          .validateAndWrapResult() shouldBe Left(ErrorWrapper(correlationId, MissingToDateError))
      }
    }

    "the businessType is missing when a businessId is provided" must {
      "return a MissingTypeOfBusinessError" in {
        validatorFactory
          .validator(validNino, None, Some(validBusinessId), Some(validFromDate), Some(validToDate), Some(validStatus))
          .validateAndWrapResult() shouldBe Left(ErrorWrapper(correlationId, MissingTypeOfBusinessError))
      }
    }

    "the toDate is later than 2100" must {
      "return a ToDateFormatError error" in {
        validatorFactory
          .validator(
            nino = validNino,
            typeOfBusiness = Some(validTypeOfBusiness),
            businessId = Some(validBusinessId),
            fromDate = Some("2099-12-24"),
            toDate = Some("2100-07-01"),
            status = Some(validStatus)
          )
          .validateAndWrapResult() shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
      }
    }

    def test(nino: String, typeOfBusiness: String, businessId: String, fromDate: String, toDate: String, status: String, error: MtdError): Unit = {
      s"return ${error.code} error" when {
        s"validating with ($nino, $typeOfBusiness, $businessId, $fromDate, $toDate, $status) is supplied" in {
          validatorFactory
            .validator(nino, Some(typeOfBusiness), Some(businessId), Some(fromDate), Some(toDate), Some(status))
            .validateAndWrapResult() shouldBe
            Left(ErrorWrapper(correlationId, error))
        }
      }
    }

    Seq(
      ("notANino", validTypeOfBusiness, validBusinessId, validFromDate, validToDate, validStatus, NinoFormatError),
      (validNino, "notABusinessType", validBusinessId, validFromDate, validToDate, validStatus, TypeOfBusinessFormatError),
      (validNino, validTypeOfBusiness, "notABusinessId", validFromDate, validToDate, validStatus, BusinessIdFormatError),
      (validNino, validTypeOfBusiness, validBusinessId, "notADate", validToDate, validStatus, FromDateFormatError),
      (validNino, validTypeOfBusiness, validBusinessId, validToDate, "notADate", validStatus, ToDateFormatError),
      (validNino, validTypeOfBusiness, validBusinessId, "2017-01-01", "2018-01-01", validStatus, RuleFromDateNotSupportedError),
      (validNino, validTypeOfBusiness, validBusinessId, "2019-01-01", "2018-01-01", validStatus, ToDateBeforeFromDateError),
      (validNino, validTypeOfBusiness, validBusinessId, "2020-01-01", "2020-01-01", validStatus, RuleDateRangeInvalidError),
      (validNino, validTypeOfBusiness, validBusinessId, "2018-12-12", "2020-04-05", validStatus, RuleDateRangeInvalidError),
      (validNino, validTypeOfBusiness, validBusinessId, validFromDate, validToDate, "notAStatus", StatusFormatError)
    ).foreach(test.tupled)

    "request supplied has multiple errors" must {
      "return multiple errors" in {
        inside(validatorFactory.validator("bad", Some("bad"), Some("bad"), Some("bad"), Some("bad"), Some("bad")).validateAndWrapResult()) {
          case Left(ErrorWrapper(_, BadRequestError, Some(errs))) =>
            errs should contain theSameElementsAs Seq(
              NinoFormatError,
              BusinessIdFormatError,
              FromDateFormatError,
              ToDateFormatError,
              StatusFormatError,
              TypeOfBusinessFormatError
            )
        }
      }
    }

  }

}
