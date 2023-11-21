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

package v1.controllers.validators.resolvers

import api.controllers.validators.resolvers.{ResolveIsoDate, ResolverSupport}
import api.models.domain.DateRange
import api.models.errors._
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._

/** Simple date range resolver for optional date ranges.
  *
  * Requires the presence of neither or both of them and that each date supplied is correctly formatted. Does not validate date order.
  */
object ResolveOptionalDateRange extends ResolverSupport {

  private val fromDateResolver = ResolveIsoDate(FromDateFormatError).resolver
  private val toDateResolver   = ResolveIsoDate(ToDateFormatError).resolver

  val resolver: Resolver[(Option[String], Option[String]), Option[DateRange]] = {
    case (Some(fromString), Some(toString)) =>
      (
        fromDateResolver(fromString),
        toDateResolver(toString)
      ).mapN(DateRange).map(Some(_))

    case (None, None)    => Valid(None)
    case (None, Some(_)) => Invalid(List(MissingFromDateError))
    case (Some(_), None) => Invalid(List(MissingToDateError))
  }

}
