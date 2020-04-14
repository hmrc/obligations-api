/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.request.retrievePeriodObligations

import uk.gov.hmrc.domain.Nino
import v1.models.domain.Statuses
import v1.models.domain.business.MtdBusinesses

case class RetrievePeriodicObligationsRequest(nino: Nino,
                                              typeOfBusiness: Option[MtdBusinesses],
                                              incomeSourceId: Option[String],
                                              fromDate: Option[String],
                                              toDate: Option[String],
                                              status: Option[Statuses])
