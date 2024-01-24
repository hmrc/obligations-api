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

package v2.models.response.common

import api.models.domain.business.{DesBusiness, MtdBusiness}
import api.models.domain.status.{DesStatus, MtdStatus}

trait FixtureDefaults {

  object Defaults {
    val incomeSourceType: DesBusiness = DesBusiness.ITSB
    val typeOfBusiness: MtdBusiness   = MtdBusiness.`self-employment`

    val referenceType      = "MTDBIS"
    val referenceNumber    = "someBusinessId"
    val businessId: String = referenceNumber

    val fromDate     = "2000-01-01"
    val toDate       = "2000-02-02"
    val receivedDate = "2000-03-03"
    val dueDate      = "2000-04-04"
    val periodKey    = "somePeriod"

    val desStatus: DesStatus = DesStatus.O
    val mtdStatus: MtdStatus = MtdStatus.Open

  }

}
