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

package v2.models.response.retrieveCrystallisationObligations.des

import api.models.domain.PeriodKey
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Reads }
import v2.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse

case class DesRetrieveCrystallisationObligationsResponse(obligationDetails: Seq[DesObligation]) {

  def toMtd: RetrieveCrystallisationObligationsResponse = RetrieveCrystallisationObligationsResponse(
    obligations = obligationDetails.map(_.toMtd)
  )
}

object DesRetrieveCrystallisationObligationsResponse {

  //bound case class to allow us to read from multiple lists of obligation details to merge together later
  case class Detail(incomeSourceType: Option[String], obligation: Seq[DesObligation])

  object Detail {
    implicit val reads: Reads[Detail] = (
      (JsPath \ "identification" \ "incomeSourceType").readNullable[String] and
        (JsPath \ "obligationDetails").read[Seq[DesObligation]]
    )(Detail.apply _)
  }

  implicit val reads: Reads[DesRetrieveCrystallisationObligationsResponse] = {
    (JsPath \ "obligations")
      .read[Seq[Detail]]
      .map(_.filter(_.incomeSourceType.contains(PeriodKey.ITSA.toString)))
      .map(det => DesRetrieveCrystallisationObligationsResponse(det.flatMap(_.obligation))) // flatten nested arrays into one array
  }
}
