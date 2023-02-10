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

package api.models.domain.business

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait MtdBusiness {
  def toDes: DesBusiness
}

object MtdBusiness {
  case object `self-employment` extends MtdBusiness {
    override def toDes: DesBusiness = DesBusiness.ITSB
  }
  case object `uk-property` extends MtdBusiness {
    override def toDes: DesBusiness = DesBusiness.ITSP
  }
  case object `foreign-property` extends MtdBusiness {
    override def toDes: DesBusiness = DesBusiness.ITSF
  }
  case object `do-not-use` extends MtdBusiness {
    override def toDes: DesBusiness = DesBusiness.ITSA
  }

  implicit val format: Format[MtdBusiness]         = Enums.format[MtdBusiness]
  val parser: PartialFunction[String, MtdBusiness] = Enums.parser[MtdBusiness]
}
