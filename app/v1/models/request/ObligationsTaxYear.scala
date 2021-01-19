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

package v1.models.request

import java.time.LocalDate

/**
  * Represents a tax year for DES
  *
  * @param from the from date string (where YYXX-ZZ is translated to 20XX-04-06)
  * @param to the to date string (where YYXX-ZZ is translated to 20ZZ-04-05)
  */
case class ObligationsTaxYear(from: String, to: String)

trait ObligationsTaxYearHelpers {

  val date: LocalDate = LocalDate.now()

  /**
    * @param taxYear tax year in MTD format (e.g. 2017-18)
    */
  case class RawTaxYear(taxYear: Option[String]) {
    val pattern = "20([1-9][0-9])\\-([1-9][0-9])"

    require(taxYear.forall(_.matches(pattern)))

    /**
      * @return ObligationsTaxYear model, where the from and to date turn YYXX-ZZ to 20XX-04-06 and 20ZZ-04-05
      */
    def toObligationsTaxYear: ObligationsTaxYear = {
      val patternRegex = pattern.r
      val patternRegex(fromYear, toYear) = taxYear.getOrElse(getMostRecentTaxYear)
      ObligationsTaxYear(s"20$fromYear-04-06", s"20$toYear-04-05")
    }
  }

  /**
    * @param year (e.g. 2020)
    *
    * @return tax year in MTD format (e.g. 2020 -> 2019-20)
    */
  private def returnYearInMtdFormat(year: Int): String = {
    (year - 1).toString + "-" + year.toString.drop(2)
  }

  /**
    * @return the most recent complete tax year. If the date is before or on 5th April,
    *         the year is not considered complete and the previous tax year will be returned
    */
  def getMostRecentTaxYear: String = {
    val year = date.getYear
    val fiscalYearStartDate = LocalDate.parse(s"$year-04-05")

    if(date.isAfter(fiscalYearStartDate)) returnYearInMtdFormat(year)
    else returnYearInMtdFormat(year - 1)
  }
}
