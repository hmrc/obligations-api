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

import api.models.domain.ReferenceType
import api.models.domain.business.MtdBusiness
import play.api.libs.json.{Json, OWrites}
import v1.models.response.downstream.DownstreamObligation

case class BusinessObligation(typeOfBusiness: MtdBusiness, businessId: String, obligationDetails: Seq[ObligationDetail])

object BusinessObligation {
  implicit val writes: OWrites[BusinessObligation] = Json.writes[BusinessObligation]

  /** Note: this returns an Option because:
    *   - it requires a DownstreamObligation with an identification which
    *     - must have a referenceType of MTDBIS
    *   - it must have an incomeSourceType
    *   - and the incomeSourceType must correspond to an MtdBusiness type.
    */
  def fromDownstream(downstreamObligation: DownstreamObligation): Option[BusinessObligation] =
    for {
      identification <- downstreamObligation.identification if identification.referenceType == ReferenceType.MTDBIS
      desType        <- identification.incomeSourceType
      mtdType        <- desType.toMtd
    } yield BusinessObligation(
      typeOfBusiness = mtdType,
      businessId = identification.referenceNumber,
      obligationDetails = downstreamObligation.obligationDetails.map(ObligationDetail.fromDownstream)
    )

}
