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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class DateRange(startDate: LocalDate, endDate: LocalDate) {
  val startDateAsIso: String = startDate.format(DateTimeFormatter.ISO_DATE)
  val endDateAsIso: String   = endDate.format(DateTimeFormatter.ISO_DATE)
}

object DateRange {
  def parse(from: String, to: String): DateRange = DateRange(LocalDate.parse(from), LocalDate.parse(to))
}
