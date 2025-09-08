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

package v2.retrievePeriodic

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.ResolverSupport.*
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino}
import api.models.domain.status.MtdStatus
import api.models.errors.*
import cats.data.Validated
import cats.implicits.*
import v2.controllers.validators.resolvers.{ObligationsDateRangeSupport, ResolveMtdBusiness, ResolveMtdStatus, ResolveOptionalDateRange}
import v2.retrievePeriodic.model.request.RetrievePeriodicObligationsRequest

import java.time.Clock
import javax.inject.{Inject, Singleton}

@Singleton
class RetrievePeriodicObligationsValidatorFactory @Inject() (implicit clock: Clock) {
  private val resolveTypeOfBusiness = ResolveMtdBusiness.resolver.resolveOptionally
  private val resolveBusinessId     = ResolveBusinessId.resolver.resolveOptionally
  private val resolveStatus         = ResolveMtdStatus.resolver.resolveOptionally
  private val resolveDateRange      = ResolveOptionalDateRange.resolver

  def validator(nino: String,
                typeOfBusiness: Option[String],
                businessId: Option[String],
                fromDate: Option[String],
                toDate: Option[String],
                status: Option[String]): Validator[RetrievePeriodicObligationsRequest] =
    new Validator[RetrievePeriodicObligationsRequest] {

      def validate: Validated[Seq[MtdError], RetrievePeriodicObligationsRequest] =
        (
          ResolveNino(nino),
          resolveTypeOfBusiness(typeOfBusiness),
          resolveBusinessId(businessId),
          resolveDateRange((fromDate, toDate)),
          resolveStatus(status)
        ).mapN(RetrievePeriodicObligationsRequest.apply)
          .andThen(validateRules)
          .map(provideDefaultDateRange)

    }

  private def provideDefaultDateRange(request: RetrievePeriodicObligationsRequest): RetrievePeriodicObligationsRequest = {
    lazy val requestWithDefaultDateRange = request.copy(dateRange = Some(ObligationsDateRangeSupport.defaultDateRange))

    request.dateRange match {
      case None if !request.status.contains(MtdStatus.Open) => requestWithDefaultDateRange
      case _                                                => request
    }
  }

  private val validateRules = {
    val validateMissingBusinessType = { (request: RetrievePeriodicObligationsRequest) =>
      import request.*

      (businessId, typeOfBusiness) match {
        case (Some(_), None) => Some(List(MissingTypeOfBusinessError))
        case _               => None
      }
    }

    val validateDateRange = ObligationsDateRangeSupport.validator.validateOptionally
      .contramap[RetrievePeriodicObligationsRequest](_.dateRange)

    resolveValid[RetrievePeriodicObligationsRequest]
      .thenValidate(validateMissingBusinessType)
      .thenValidate(validateDateRange)
  }

}
