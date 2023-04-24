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

package v1.connectors

import api.connectors.{ BaseDownstreamConnector, DownstreamOutcome }
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.DownstreamUri._
import api.models.domain.status.MtdStatus
import config.AppConfig
import javax.inject.{ Inject, Singleton }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import v1.models.request.retrievePeriodObligations.RetrievePeriodicObligationsRequest
import v1.models.response.retrievePeriodicObligations.RetrievePeriodObligationsResponse

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class RetrievePeriodicObligationsConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrievePeriodicObligations(request: RetrievePeriodicObligationsRequest)(
      implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrievePeriodObligationsResponse]] = {

    import request._

    val queryParams: Seq[(String, String)] = Seq(
      "from"   -> fromDate,
      "to"     -> toDate,
      "status" -> status
    ).collect {
      case (k, Some(v: MtdStatus)) => k -> v.toDes.toString
      case (k, Some(v: String))    => k -> v
    }

    val url = DesUri[RetrievePeriodObligationsResponse](s"enterprise/obligation-data/nino/$nino/ITSA")

    get(url, queryParams)
  }
}
