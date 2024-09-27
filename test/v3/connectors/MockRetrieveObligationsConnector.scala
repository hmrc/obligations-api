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

package v3.connectors

import api.connectors.DownstreamOutcome
import api.models.domain.status.MtdStatusV3
import api.models.domain.{DateRange, Nino}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v3.models.response.downstream.DownstreamObligations

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveObligationsConnector extends MockFactory {
  val mockRetrieveObligationsConnector: RetrieveObligationsConnector = mock[RetrieveObligationsConnector]

  object MockRetrieveObligationsConnector {

    def retrieveObligations(nino: Nino,
                            dateRange: Option[DateRange],
                            status: Option[MtdStatusV3]): CallHandler[Future[DownstreamOutcome[DownstreamObligations]]] =
      (
        mockRetrieveObligationsConnector
          .retrieveObligations(_: Nino, _: Option[DateRange], _: Option[MtdStatusV3])(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: String
          )
        )
        .expects(nino, dateRange, status, *, *, *)

  }

}
