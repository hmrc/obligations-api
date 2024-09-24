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

sealed trait DesStatusV3 {
  def toMtd: MtdStatusV3
}

object DesStatusV3 {
  val parser: PartialFunction[String, DesStatusV3] = Enums.parser[DesStatusV3]

  case object F extends DesStatusV3 {
    override def toMtd: MtdStatusV3 = MtdStatusV3.fulfilled
  }

  implicit val format: Format[DesStatusV3] = Enums.format[DesStatusV3]

  case object O extends DesStatusV3 {
    override def toMtd: MtdStatusV3 = MtdStatusV3.open
  }

}
