/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.domain.status

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait DesStatus {
  def toMtd: MtdStatus
}

object DesStatus {
  case object F extends DesStatus {
    override def toMtd: MtdStatus = MtdStatus.Fulfilled
  }
  case object O extends DesStatus {
    override def toMtd: MtdStatus = MtdStatus.Open
  }

  implicit val format: Format[DesStatus] = Enums.format[DesStatus]
  val parser: PartialFunction[String, DesStatus] = Enums.parser[DesStatus]
}
