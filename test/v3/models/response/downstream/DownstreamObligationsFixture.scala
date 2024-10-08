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

package v3.models.response.downstream

import api.models.domain.business.DesBusiness
import api.models.domain.status.DesStatusV3
import v3.models.response.domain.FixtureDefaults

trait DownstreamObligationsFixture extends FixtureDefaults {

  def downstreamIdentification(incomeSourceType: Option[DesBusiness] = Some(Defaults.incomeSourceType),
                               referenceNumber: String = Defaults.referenceNumber,
                               referenceType: String = Defaults.referenceType): DownstreamIdentification =
    DownstreamIdentification(incomeSourceType, referenceNumber, referenceType)

  def downstreamObligationDetail(status: DesStatusV3 = Defaults.desStatus,
                                 inboundCorrespondenceFromDate: String = Defaults.fromDate,
                                 inboundCorrespondenceToDate: String = Defaults.toDate,
                                 inboundCorrespondenceDateReceived: Option[String] = Some(Defaults.receivedDate),
                                 inboundCorrespondenceDueDate: String = Defaults.dueDate,
                                 periodKey: String = Defaults.periodKey): DownstreamObligationDetail =
    DownstreamObligationDetail(
      status,
      inboundCorrespondenceFromDate,
      inboundCorrespondenceToDate,
      inboundCorrespondenceDateReceived,
      inboundCorrespondenceDueDate,
      periodKey)

  def downstreamObligation(identification: Option[DownstreamIdentification],
                           obligationDetails: Seq[DownstreamObligationDetail]): DownstreamObligation =
    DownstreamObligation(identification, obligationDetails = obligationDetails)

}
