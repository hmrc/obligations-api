/*
 * Copyright 2021 HM Revenue & Customs
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

import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.{FromDateFormatError, MtdError, ToDateFormatError}
import v1.models.request.retrievePeriodObligations.RetrievePeriodicObligationsRawData

class RetrievePeriodicObligationsValidator extends Validator[RetrievePeriodicObligationsRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, missingParameterValidation)

  private def parameterFormatValidation: RetrievePeriodicObligationsRawData => List[List[MtdError]] = data => {
    List(
      NinoValidation.validate(data.nino),
      data.businessId.map(BusinessIdValidation.validate).getOrElse(Nil),
      data.fromDate.map(DateValidation.validate(_, FromDateFormatError)).getOrElse(Nil),
      data.toDate.map(DateValidation.validate(_, ToDateFormatError)).getOrElse(Nil),
      data.status.map(StatusValidation.validate).getOrElse(Nil),
      data.typeOfBusiness.map(TypeOfBusinessValidation.validate).getOrElse(Nil)
    )
  }

  private def parameterRuleValidation: RetrievePeriodicObligationsRawData => List[List[MtdError]] = (data: RetrievePeriodicObligationsRawData) => {
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

  private def missingParameterValidation: RetrievePeriodicObligationsRawData => List[List[MtdError]] = (data: RetrievePeriodicObligationsRawData) => {
    List(
      DateMissingValidation.validate(data.fromDate, data.toDate),
      BusinessIdIncludedWithTypeOfBusinessValidation.validate(data.businessId, data.typeOfBusiness)
    )
  }

  override def validate(data: RetrievePeriodicObligationsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
