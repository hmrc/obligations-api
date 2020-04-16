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

package v1.models.response.retrievePeriodicObligations

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.domain.PeriodKey
<<<<<<< HEAD:app/v1/models/response/retrievePeriodicObligations/RetrievePeriodObligationsResponse.scala
import v1.models.response.retrievePeriodicObligations.des.DesObligation
=======
import v1.models.response.common
import v1.models.response.common.{Obligation, ObligationDetail}
import v1.models.response.common.des.{DesObligation, DesObligationDetail}
>>>>>>> MTDSA-5237 Models for EOPS Obligations:app/v1/models/response/retrievePeriodObligations/RetrievePeriodObligationsResponse.scala

case class RetrievePeriodObligationsResponse(obligations: Seq[Obligation])

object RetrievePeriodObligationsResponse {

  implicit val reads: Reads[RetrievePeriodObligationsResponse] = {
    (JsPath \ "obligations").read[Seq[DesObligation]].map( // go inside Reads
      _.map { // go inside Seq
        ob =>
          common.Obligation(
            ob.incomeSourceType.toMtd,
            ob.referenceNumber,
            ob.obligationDetails.collect {
              case det if (det.periodKey != PeriodKey.EOPS.toString && det.periodKey != PeriodKey.ITSA.toString) =>
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
    ).map(RetrievePeriodObligationsResponse(_))
  }

  implicit val writes: OWrites[RetrievePeriodObligationsResponse] = Json.writes[RetrievePeriodObligationsResponse]
}