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

package v1.models.response.retrieveEOPSObligations

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.domain.PeriodKey
import v1.models.domain.business.DesBusiness
import v1.models.response.common.des.DesObligation
import v1.models.response.common.{Obligation, ObligationDetail}

case class RetrieveEOPSObligationsResponse(obligations: Seq[Obligation])

object RetrieveEOPSObligationsResponse {

  implicit val reads: Reads[RetrieveEOPSObligationsResponse] = {
    (JsPath \ "obligations").read[Seq[DesObligation]].map( // go inside Reads
      _.collect { // go inside Seq
        case ob if (ob.incomeSourceType != DesBusiness.ITSA) =>
          Obligation(
            ob.incomeSourceType.toMtd,
            ob.referenceNumber,
            ob.obligationDetails.collect {
              case det if (det.periodKey == PeriodKey.EOPS.toString) =>
                ObligationDetail(
                  det.inboundCorrespondenceFromDate,
                  det.inboundCorrespondenceToDate,
                  det.inboundCorrespondenceDueDate,
                  det.inboundCorrespondenceDateReceived,
                  det.status.toMtd
                )
            }
          )
      }
    ).map(RetrieveEOPSObligationsResponse(_))
  }


  implicit val writes: OWrites[RetrieveEOPSObligationsResponse] = Json.writes[RetrieveEOPSObligationsResponse]
}
