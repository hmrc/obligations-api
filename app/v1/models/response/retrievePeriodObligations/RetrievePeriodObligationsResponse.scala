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

package v1.models.response.retrievePeriodObligations

import play.api.libs.json.{JsPath, Reads}
import v1.models.domain.PeriodKey
import v1.models.response.retrieveCrystallisationObligations.des.DesObligation
import v1.models.response.retrievePeriodObligations.des.DesBusiness

case class RetrievePeriodObligationsResponse(obligations: Seq[Business])

object RetrievePeriodObligationsResponse {

  case class Detail(obligations: Seq[Business])

  object Detail{
   implicit val reads: Reads[Detail] =
     (JsPath \ "obligations").read[Seq[DesBusiness]].map(_.collect {
       case u =>
         Business(
           typeOfBusiness = u.incomeSourceType.toMtd,
           businessId = u.referenceNumber,
           obligationDetails = Obligation((JsPath \ "obligations" \ "obligationDetails").read[Seq[DesObligation]]
             .map(
               _.collect {
                 case o if o.periodKey != PeriodKey.ITSA.toString || o.periodKey != PeriodKey.EPOS.toString =>
                   Obligation(
                     periodKey = Some(o.periodKey),
                     periodStartDate = o.inboundCorrespondenceFromDate,
                     periodEndDate = o.inboundCorrespondenceToDate,
                     dueDate = o.inboundCorrespondenceDueDate,
                     status = o.status.toMtd,
                     receivedDate = o.inboundCorrespondenceDateReceived
                   )
               }
             )
           )
         )
        }
     )
       .map(Detail(_))
  }
  implicit val reads: Reads[RetrievePeriodObligationsResponse] = {
    (JsPath \ "obligations").read[Seq[Detail]]
      .map(det => RetrievePeriodObligationsResponse(det.flatMap(_.obligations)))
  }

  implicit val writes: OWrites[RetrievePeriodObligationsResponse] = Json.writes[RetrievePeriodObligationsResponse]
}