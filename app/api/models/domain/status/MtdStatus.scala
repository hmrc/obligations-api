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

package api.models.domain.status

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait MtdStatus {
  def toDes: DesStatus
}

object MtdStatus {
  val parser: PartialFunction[String, MtdStatus] = Enums.parser[MtdStatus]

  case object Fulfilled extends MtdStatus {
    override def toDes: DesStatus = DesStatus.F
  }

  implicit val format: Format[MtdStatus] = Enums.format[MtdStatus]

  case object Open extends MtdStatus {
    override def toDes: DesStatus = DesStatus.O
  }

}
