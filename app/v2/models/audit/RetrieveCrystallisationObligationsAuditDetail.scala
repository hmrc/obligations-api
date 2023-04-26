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

package v2.models.audit

import api.models.audit.AuditResponse
import api.models.auth.UserDetails
import play.api.libs.json.{Json, Writes}

case class RetrieveCrystallisationObligationsAuditDetail(userType: String,
                                                         agentReferenceNumber: Option[String],
                                                         nino: String,
                                                         taxYear: Option[String],
                                                         `X-CorrelationId`: String,
                                                         response: AuditResponse)

object RetrieveCrystallisationObligationsAuditDetail {
  implicit val writes: Writes[RetrieveCrystallisationObligationsAuditDetail] = Json.writes[RetrieveCrystallisationObligationsAuditDetail]

  def apply(userDetails: UserDetails,
            nino: String,
            taxYear: Option[String],
            `X-CorrelationId`: String,
            auditResponse: AuditResponse): RetrieveCrystallisationObligationsAuditDetail = {

    RetrieveCrystallisationObligationsAuditDetail(
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      nino = nino,
      taxYear = taxYear,
      `X-CorrelationId` = `X-CorrelationId`,
      response = auditResponse
    )
  }
}
