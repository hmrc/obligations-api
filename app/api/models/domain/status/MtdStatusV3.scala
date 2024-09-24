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

sealed trait MtdStatusV3 {
  def toDes: DesStatusV3
}

object MtdStatusV3 {
  val parser: PartialFunction[String, MtdStatusV3] = Enums.parser[MtdStatusV3]

  case object fulfilled extends MtdStatusV3 {
    override def toDes: DesStatusV3 = DesStatusV3.F
  }

  implicit val format: Format[MtdStatusV3] = Enums.format[MtdStatusV3]

  case object open extends MtdStatusV3 {
    override def toDes: DesStatusV3 = DesStatusV3.O
  }

}
