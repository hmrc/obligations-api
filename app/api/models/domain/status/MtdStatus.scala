/*
 * Copyright 2026 HM Revenue & Customs
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

enum MtdStatus(val toDownstream: String) {
  case fulfilled extends MtdStatus("F")
  case open      extends MtdStatus("O")
}

object MtdStatus {
  val parser: PartialFunction[String, MtdStatus] = Enums.parser(values)

  implicit val format: Format[MtdStatus] = Enums.format(values)
}
