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

package v1.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.ResolverSupport._
import api.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum}
import api.models.domain.TaxYear
import api.models.errors.{MtdError, RuleTaxYearRangeExceededError, RuleTaxYearRangeInvalidError}
import cats.data.Validated
import cats.implicits._
import v1.models.request.ObligationsTaxYearHelpers
import v1.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest

import javax.inject.Singleton

@Singleton
class RetrieveCrystallisationObligationsValidatorFactory extends ObligationsTaxYearHelpers {

  private val minTaxYear: TaxYear = TaxYear.fromMtd("2017-18")

  private val resolveTaxYear = ResolveTaxYearMinimum(minTaxYear).resolver.resolveOptionally
    .map(maybeTaxYear => RawTaxYear(maybeTaxYear.map(_.asMtd)).toObligationsTaxYear)

  def validator(nino: String, taxYear: Option[String]): Validator[RetrieveCrystallisationObligationsRequest] =
    new Validator[RetrieveCrystallisationObligationsRequest] {

      def validate: Validated[Seq[MtdError], RetrieveCrystallisationObligationsRequest] =
        (
          ResolveNino(nino),
          resolveTaxYear(taxYear).leftMap { errs =>
            errs.map {
              case RuleTaxYearRangeInvalidError => RuleTaxYearRangeExceededError
              case err                          => err
            }
          }
        ).mapN(RetrieveCrystallisationObligationsRequest)

    }

}
