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

package v1.models.response.common

import api.models.domain.business.DesBusiness
import v1.models.response.downstream.DownstreamObligation

case class ITSAObligation(obligationDetails: Seq[ObligationDetail])

object ITSAObligation {

  /** Note: this returns an Option because:
    *   - it requires a DownstreamObligation with an identification which
    *   - must have an incomeSourceType with a value ITSA.
    */
  def fromDownstream(downstreamObligation: DownstreamObligation): Option[ITSAObligation] =
    for {
      identification   <- downstreamObligation.identification
      incomeSourceType <- identification.incomeSourceType if incomeSourceType == DesBusiness.ITSA
    } yield ITSAObligation(
      obligationDetails = downstreamObligation.obligationDetails.map(ObligationDetail.fromDownstream)
    )

}
