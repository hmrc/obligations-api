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

import v1.controllers.requestParsers.validators.validations.{DateMissingValidation, DateRangeValidation, DateValidation, IncomeSourceIdIncludedWithTypeOfBusinessValidation, IncomeSourceIdValidation, NinoValidation, StatusValidation, TypeOfBusinessValidation}
import v1.models.errors.{FromDateFormatError, MtdError, RuleDateRangeInvalidError, ToDateFormatError}
import v1.models.request.retrieveEOPSObligations.RetrieveEOPSObligationsRawData

class RetrieveEOPSObligationsValidator extends Validator[RetrieveEOPSObligationsRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, missingParameterValidation)

  private def parameterFormatValidation: RetrieveEOPSObligationsRawData => List[List[MtdError]] = data => {
    List(
      NinoValidation.validate(data.nino),
      data.incomeSourceId.map(IncomeSourceIdValidation.validate).getOrElse(Nil),
      data.fromDate.map(DateValidation.validate(_, FromDateFormatError)).getOrElse(Nil),
      data.toDate.map(DateValidation.validate(_, ToDateFormatError)).getOrElse(Nil),
      data.status.map(StatusValidation.validate).getOrElse(Nil),
      data.typeOfBusiness.map(TypeOfBusinessValidation.validate).getOrElse(Nil),
      IncomeSourceIdIncludedWithTypeOfBusinessValidation.validate(data.incomeSourceId, data.typeOfBusiness)
    )
  }

  private def parameterRuleValidation: RetrieveEOPSObligationsRawData => List[List[MtdError]] = (data: RetrieveEOPSObligationsRawData) => {
    val dateRangeValidation = for {
      fromDate <- data.fromDate
      toDate <- data.toDate
    } yield {
      DateRangeValidation.validate(fromDate, toDate)
    }

    List(
      dateRangeValidation.getOrElse(Nil)
    )
  }

  private def missingParameterValidation: RetrieveEOPSObligationsRawData => List[List[MtdError]] = (data: RetrieveEOPSObligationsRawData) => {
    List(
      DateMissingValidation.validate(data.fromDate, data.toDate)
    )
  }

  override def validate(data: RetrieveEOPSObligationsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
