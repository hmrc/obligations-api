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

package v1.models.response.common

import api.models.domain.status.MtdStatus
import play.api.libs.json.{Json, OWrites}
import v1.models.response.downstream.DownstreamObligationDetail

case class ObligationDetail(
    periodStartDate: String,
    periodEndDate: String,
    dueDate: String,
    receivedDate: Option[String],
    status: MtdStatus
)

object ObligationDetail {
  implicit val writes: OWrites[ObligationDetail] = Json.writes[ObligationDetail]

  def fromDownstream(downstreamObligationDetail: DownstreamObligationDetail): ObligationDetail = {
    import downstreamObligationDetail._
    ObligationDetail(
      periodStartDate = inboundCorrespondenceFromDate,
      periodEndDate = inboundCorrespondenceToDate,
      dueDate = inboundCorrespondenceDueDate,
      receivedDate = inboundCorrespondenceDateReceived,
      status = status.toMtd
    )
  }

}
