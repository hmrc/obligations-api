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

package v2.services

import api.controllers.RequestContextImplicits
import api.models.domain.BusinessId
import api.models.domain.business.MtdBusiness
import utils.Logging
import v2.models.response.domain.BusinessObligation
import v2.models.response.downstream.{DownstreamObligationDetail, DownstreamObligations}

trait BaseService extends RequestContextImplicits with DownstreamResponseMappingSupport with Logging {

  protected def toMtdBusinessObligations(downstreamObligations: DownstreamObligations,
                                         typeOfBusiness: Option[MtdBusiness],
                                         businessId: Option[BusinessId])(
      detailsPredicate: DownstreamObligationDetail => Boolean): Seq[BusinessObligation] = {

    def matchesBusiness(obligation: BusinessObligation): Boolean =
      typeOfBusiness.forall(_ == obligation.typeOfBusiness) && businessId.forall(_.value == obligation.businessId)

    for {
      dsOb  <- downstreamObligations.obligations.map(_.filterObligationDetails(detailsPredicate))
      mtdOb <- BusinessObligation.fromDownstream(dsOb) if matchesBusiness(mtdOb) && mtdOb.obligationDetails.nonEmpty
    } yield mtdOb
  }

}
