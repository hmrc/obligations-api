/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.models.response.domain

import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatus

trait ObligationsFixture extends FixtureDefaults {

  def obligationDetail(periodStartDate: String = Defaults.fromDate,
                       periodEndDate: String = Defaults.toDate,
                       dueDate: String = Defaults.dueDate,
                       receivedDate: Option[String] = Some(Defaults.receivedDate),
                       status: MtdStatus = Defaults.mtdStatus): ObligationDetail =
    ObligationDetail(
      periodStartDate = periodStartDate,
      periodEndDate = periodEndDate,
      dueDate = dueDate,
      receivedDate = receivedDate,
      status = status
    )

  def obligation(typeOfBusiness: MtdBusiness = Defaults.typeOfBusiness,
                 businessId: String = Defaults.businessId,
                 obligationDetails: Seq[ObligationDetail]): BusinessObligation =
    BusinessObligation(typeOfBusiness = typeOfBusiness, businessId = businessId, obligationDetails = obligationDetails)

}
