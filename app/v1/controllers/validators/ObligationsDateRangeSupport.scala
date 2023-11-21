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

import api.controllers.validators.resolvers.ResolverSupport
import api.models.domain.DateRange
import api.models.errors._

import java.time.{Clock, LocalDate}
import scala.math.Ordering.Implicits.infixOrderingOps

object ObligationsDateRangeSupport extends ResolverSupport {

  private val earliestFromDate: LocalDate = LocalDate.parse("2018-04-06")
  private val latestToDate: LocalDate     = LocalDate.parse("2099-12-31")
  private val maxDateRange: Int           = 366

  val validator: Validator[DateRange] =
    combinedValidator(
      satisfies(ToDateBeforeFromDateError)(range => range.endDate >= range.startDate),
      satisfiesMin(earliestFromDate, RuleFromDateNotSupportedError).contramap(_.startDate),
      satisfiesMax(latestToDate, ToDateFormatError).contramap(_.endDate),
      satisfies(RuleDateRangeInvalidError)(range => range.endDate < range.startDate.plusDays(maxDateRange)),
      satisfies(RuleDateRangeInvalidError)(range => range.endDate != range.startDate)
    )

  def defaultDateRange(implicit clock: Clock = Clock.systemUTC): DateRange = {
    val now = LocalDate.now(clock)
    DateRange(now, now.plusDays(maxDateRange))
  }

}
