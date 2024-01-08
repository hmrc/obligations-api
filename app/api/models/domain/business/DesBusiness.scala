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

sealed trait DesBusiness {
  def toMtd: Option[MtdBusiness]
}

object DesBusiness {
  val parser: PartialFunction[String, DesBusiness] = Enums.parser[DesBusiness]
  implicit val format: Format[DesBusiness]         = Enums.format[DesBusiness]

  case object ITSB extends DesBusiness {

    override def toMtd: Option[MtdBusiness] = Some(
      MtdBusiness.`self-employment`
    )

  }

  case object ITSP extends DesBusiness {
    override def toMtd: Option[MtdBusiness] = Some(MtdBusiness.`uk-property`)
  }

  case object ITSF extends DesBusiness {

    override def toMtd: Option[MtdBusiness] = Some(
      MtdBusiness.`foreign-property`
    )

  }

  case object ITSA extends DesBusiness {
    override def toMtd: Option[MtdBusiness] = None
  }

}
