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

package v1.models.response.retrieveCrystallisationObligations

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.domain.PeriodKey
import v1.models.response.retrieveCrystallisationObligations.des.DesObligation

case class RetrieveCrystallisationObligationsResponse(obligationDetails: Seq[Obligation])

object RetrieveCrystallisationObligationsResponse {

  //bound case class to allow us to read from multiple lists of obligation details to merge together later
  case class Detail(obligation: Seq[Obligation])

  object Detail {
    implicit val reads: Reads[Detail] = (JsPath \ "obligationDetails").read[Seq[DesObligation]]
      .map(
        _.collect {
          case o if o.periodKey == PeriodKey.ITSA.toString =>
            Obligation(
              periodStartDate = o.inboundCorrespondenceFromDate,
              periodEndDate = o.inboundCorrespondenceToDate,
              dueDate = o.inboundCorrespondenceDueDate,
              status = o.status.toMtd,
              receivedDate = o.inboundCorrespondenceDateReceived
            )
        }
      )
      .map(Detail(_))
  }

  implicit val reads: Reads[RetrieveCrystallisationObligationsResponse] = {
    (JsPath \ "obligations").read[Seq[Detail]]
      .map(det => RetrieveCrystallisationObligationsResponse(det.flatMap(_.obligation))) // flatten nested arrays into one array
  }

  implicit val writes: OWrites[RetrieveCrystallisationObligationsResponse] = Json.writes[RetrieveCrystallisationObligationsResponse]
}
