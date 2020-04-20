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

package v1.controllers.requestParsers

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import v1.controllers.requestParsers.validators.RetrieveEOPSObligationsValidator
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.MtdStatus
import v1.models.request.retrieveEOPSObligations.{RetrieveEOPSObligationsRawData, RetrieveEOPSObligationsRequest}

class RetrieveEOPSObligationsRequestParser @Inject()(val validator: RetrieveEOPSObligationsValidator)
  extends RequestParser[RetrieveEOPSObligationsRawData, RetrieveEOPSObligationsRequest] {

  override protected def requestFor(data: RetrieveEOPSObligationsRawData): RetrieveEOPSObligationsRequest = {
    val typeOfBusiness: Option[MtdBusiness] = data.typeOfBusiness.map(MtdBusiness.parser)
    val status: Option[MtdStatus] = data.status.map(MtdStatus.parser)
    RetrieveEOPSObligationsRequest(Nino(data.nino), typeOfBusiness, data.incomeSourceId, data.fromDate, data.toDate, status)
  }

}
