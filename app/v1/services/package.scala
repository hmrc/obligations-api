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

package v1

import v1.models.errors.ErrorWrapper
import v1.models.outcomes.ResponseWrapper
import v1.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse
import v1.models.response.retrievePeriodicObligations.RetrievePeriodObligationsResponse

package object services {
  private type ServiceOutcome[A] = Either[ErrorWrapper, ResponseWrapper[A]]

  type RetrieveCrystallisationObligationsServiceOutcome = ServiceOutcome[RetrieveCrystallisationObligationsResponse]

  type RetrieveEOPSObligationsServiceOutcome = ServiceOutcome[RetrieveEOPSObligationsResponse]

  type RetrievePeriodicObligationsServiceOutcome = ServiceOutcome[RetrievePeriodObligationsResponse]
}
