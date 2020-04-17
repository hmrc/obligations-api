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

package v1.models.response.common.des

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import v1.models.domain.business.DesBusiness

case class DesObligation(incomeSourceType: DesBusiness,
                         referenceNumber: String,
                         referenceType: String,
                         obligationDetails: Seq[DesObligationDetail])

object DesObligation {
  implicit val reads: Reads[DesObligation] = (
    (JsPath \ "identification" \ "incomeSourceType").read[DesBusiness] and
      (JsPath \ "identification" \ "referenceNumber").read[String] and
      (JsPath \ "identification" \ "referenceType").read[String] and
      (JsPath \ "obligationDetails").read[Seq[DesObligationDetail]]
  )(DesObligation.apply _)
}