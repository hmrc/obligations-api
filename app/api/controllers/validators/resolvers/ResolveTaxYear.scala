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

package api.controllers.validators.resolvers

import api.models.domain.TaxYear
import api.models.errors.{
  InvalidTaxYearParameterError,
  MtdError,
  RuleTaxYearNotEndedError,
  RuleTaxYearNotSupportedError,
  RuleTaxYearRangeInvalidError,
  TaxYearFormatError
}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import java.time.Clock
import scala.math.Ordering.Implicits.infixOrderingOps

object ResolveTaxYear extends ResolverSupport {

  private val taxYearFormat = "20([1-9][0-9])-([1-9][0-9])".r

  val resolver: Resolver[String, TaxYear] = {
    case value @ taxYearFormat(start, end) =>
      if (end.toInt - start.toInt == 1)
        Valid(TaxYear.fromMtd(value))
      else
        Invalid(List(RuleTaxYearRangeInvalidError))

    case _ => Invalid(List(TaxYearFormatError))
  }

  def apply(value: String): Validated[Seq[MtdError], TaxYear] = resolver(value)

  // Adaptor for existing callers:
  def apply(minimumTaxYear: TaxYear, value: String): Validated[Seq[MtdError], TaxYear] = {
    val resolver = ResolveTaxYearMinimum(minimumTaxYear)

    resolver(value)
  }

}

case class ResolveTaxYearMinimum(minimumTaxYear: TaxYear) extends ResolverSupport {

  val resolver: Resolver[String, TaxYear] =
    ResolveTaxYear.resolver thenValidate satisfiesMin(minimumTaxYear, RuleTaxYearNotSupportedError)

  def apply(value: String): Validated[Seq[MtdError], TaxYear] = resolver(value)
}

case class ResolveIncompleteTaxYear(incompleteTaxYearError: MtdError = RuleTaxYearNotEndedError)(implicit clock: Clock) extends ResolverSupport {

  val resolver: Resolver[String, TaxYear] =
    ResolveTaxYear.resolver thenValidate satisfies(incompleteTaxYearError)(_ < TaxYear.currentTaxYear)

  def apply(value: String): Validated[Seq[MtdError], TaxYear] = resolver(value)
}

object ResolveTysTaxYear extends ResolverSupport {

  val resolver: Resolver[String, TaxYear] =
    ResolveTaxYear.resolver thenValidate satisfiesMin(TaxYear.tysTaxYear, InvalidTaxYearParameterError)

  def apply(value: String): Validated[Seq[MtdError], TaxYear] = resolver(value)

}
