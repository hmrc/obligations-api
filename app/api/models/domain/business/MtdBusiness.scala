/*
 * Copyright 2025 HM Revenue & Customs
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

package api.models.domain.business

import play.api.libs.json.Format
import utils.enums.Enums

enum MtdBusiness {
  case `self-employment`, `uk-property`, `foreign-property`

  def toDes: DesBusiness = this match {
    case `self-employment`  => DesBusiness.ITSB
    case `uk-property`      => DesBusiness.ITSP
    case `foreign-property` => DesBusiness.ITSF
  }

}

object MtdBusiness {
  val parser: PartialFunction[String, MtdBusiness] = Enums.parser(values)
  given Format[MtdBusiness]                        = Enums.format(values)
}
