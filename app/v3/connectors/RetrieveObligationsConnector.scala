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

package v3.connectors

import api.connectors.DownstreamUri.{DesUri, HipUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.domain.status.MtdStatus
import api.models.domain.{DateRange, Nino}
import config.{AppConfig, ConfigFeatureSwitches}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import utils.DateUtils.nowAsUtc
import v3.models.response.downstream.DownstreamObligations

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveObligationsConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveObligations(nino: Nino, dateRange: Option[DateRange], status: Option[MtdStatus])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[DownstreamObligations]] = {

    val queryParams =
      dateRange.toSeq.flatMap(range => Seq("from" -> range.startDateAsIso, "to" -> range.endDateAsIso)) ++
        status.toSeq.map("status" -> _.toDownstream)

    val hipQueryParams =
      dateRange.toSeq.flatMap(range => Seq("dateFrom" -> range.startDateAsIso, "dateTo" -> range.endDateAsIso)) ++
        status.toSeq.map("status" -> _.toDownstream)

    val additionalContractHeaders: Seq[(String, String)] = List(
      "X-Originating-System"  -> "MDTP",
      "X-Receipt-Date"        -> nowAsUtc,
      "X-Transmitting-System" -> "HIP"
    )

    lazy val hipUrl = HipUri[DownstreamObligations](s"etmp/RESTAdapter/obligation-data/nino/$nino/ITSA", additionalContractHeaders)
    lazy val desUrl = DesUri[DownstreamObligations](s"enterprise/obligation-data/nino/$nino/ITSA")

    if (ConfigFeatureSwitches().isEnabled("des_hip_migration_1330")) get(hipUrl, hipQueryParams) else get(desUrl, queryParams)
  }

}
