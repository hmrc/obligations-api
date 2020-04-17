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

package v1.models.response.retrievePeriodicObligations.des

import play.api.libs.json.{Json, Reads}
import v1.models.domain.status.DesStatus

case class DesObligationDetail(
                          inboundCorrespondenceFromDate: String,
                          inboundCorrespondenceToDate: String,
                          inboundCorrespondenceDueDate: String,
                          status: DesStatus,
                          inboundCorrespondenceDateReceived: Option[String],
                          periodKey: String
                        )

object DesObligationDetail {
  implicit val reads: Reads[DesObligationDetail] = Json.reads[DesObligationDetail]
}