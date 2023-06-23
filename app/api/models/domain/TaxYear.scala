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

package api.models.domain

import java.time.{ LocalDate, ZoneOffset }

/** Opaque representation of a tax year.
  *
  * @param value
  *   A single-year representation, e.g. "2024" represents the tax year 2023-24.
  */
final case class TaxYear private (private val value: String) {

  /** The tax year as a number, e.g. for "2023-24" this will be 2024.
    */
  val year: Int = value.toInt

  /**
    * e.g. for tax year 2023-24, "2023-04-06"
    */
  val taxYearStart: String = {
    val fromYear = year - 1
    s"$fromYear-04-06"
  }

  /**
    * e.g. for tax year 2023-24, "2024-04-05"
    */
  val taxYearEnd: String = {
    s"$year-04-05"
  }

  /** The tax year in MTD (vendor-facing) format, e.g. "2023-24".
    */
  val asMtd: String = {
    val prefix  = value.take(2)
    val yearTwo = value.drop(2)
    val yearOne = (yearTwo.toInt - 1).toString
    prefix + yearOne + "-" + yearTwo
  }

  /** The tax year in the pre-TYS downstream format, e.g. "2024".
    */
  val asDownstream: String = value

  /** The tax year in the Tax Year Specific downstream format, e.g. "23-24".
    */
  val asTysDownstream: String = {
    val year2 = value.toInt - 2000
    val year1 = year2 - 1
    s"${year1}-$year2"
  }

  /** Use this for downstream API endpoints that are known to be TYS.
    */
  def useTaxYearSpecificApi: Boolean = year >= 2024

  override def toString: String = s"TaxYear($value)"
}

object TaxYear {

  val minimumTaxYear = new TaxYear("2018")

  /** @param taxYear
    *   tax year in MTD format (e.g. 2017-18)
    */
  def fromMtd(taxYear: String): TaxYear =
    new TaxYear(taxYear.take(2) + taxYear.drop(5))

  def fromDownstream(taxYear: String): TaxYear =
    new TaxYear(taxYear)

  def fromDownstreamInt(taxYear: Int): TaxYear =
    new TaxYear(taxYear.toString)

  type TodaySupplier = () => LocalDate

  def currentTaxYear()(implicit todaySupplier: TodaySupplier = today _): TaxYear = {
    val today            = todaySupplier()
    val year             = today.getYear
    val taxYearStartDate = LocalDate.parse(s"$year-04-06")

    val taxYear =
      if (today.isBefore(taxYearStartDate)) year
      else year + 1

    new TaxYear(taxYear.toString)
  }

  def today(): LocalDate = LocalDate.now(ZoneOffset.UTC)

}
