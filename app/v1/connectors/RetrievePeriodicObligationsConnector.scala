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

package v1.connectors

import config.AppConfig
import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import v1.connectors.httpparsers.StandardDesHttpParser._
import v1.models.domain.status.MtdStatus
import v1.models.request.retrievePeriodObligations.RetrievePeriodicObligationsRequest
import v1.models.response.retrievePeriodicObligations.RetrievePeriodObligationsResponse

import scala.concurrent.{ExecutionContext, Future}

class RetrievePeriodicObligationsConnector @Inject()(val http: HttpClient,
                                                     val appConfig: AppConfig) extends BaseDesConnector {
  def retrievePeriodicObligations(request: RetrievePeriodicObligationsRequest)
                                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DesOutcome[RetrievePeriodObligationsResponse]] = {

    val queryParams: String = Seq(
      "from" -> request.fromDate,
      "to" -> request.toDate,
      "status" -> request.status
    ).collect {
      case (k, Some(v: MtdStatus)) => s"$k=${v.toDes}"
      case (k, Some(v: String)) => s"$k=$v"
    }.mkString("&")

    val url = s"enterprise/obligation-data/nino/${request.nino}/ITSA?$queryParams"

    get(
      DesUri[RetrievePeriodObligationsResponse](s"$url")
    )
  }
}
