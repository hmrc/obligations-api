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

package v2.retrieveCrystallisation

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.ResolverSupport.*
import api.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum}
import api.models.domain.{TaxYear, TaxYearRange}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.*
import v2.controllers.validators.resolvers.ResolveMtdStatus
import v2.retrieveCrystallisation.model.request.RetrieveCrystallisationObligationsRequest

import java.time.Clock
import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveCrystallisationObligationsValidatorFactory @Inject() (implicit clock: Clock) {

  private val minTaxYear: TaxYear = TaxYear.fromMtd("2017-18")

  private val resolveTaxYear =
    ResolveTaxYearMinimum(minTaxYear).resolver.map(TaxYearRange(_)).resolveOptionallyWithDefault(TaxYearRange.todayMinus(4))

  private val resolveStatus = ResolveMtdStatus.resolver.resolveOptionally

  def validator(nino: String, taxYear: Option[String], status: Option[String]): Validator[RetrieveCrystallisationObligationsRequest] =
    new Validator[RetrieveCrystallisationObligationsRequest] {

      def validate: Validated[Seq[MtdError], RetrieveCrystallisationObligationsRequest] = {

        (
          ResolveNino(nino),
          resolveTaxYear(taxYear),
          resolveStatus(status)
        ).mapN(RetrieveCrystallisationObligationsRequest.apply)
      }

    }

}
