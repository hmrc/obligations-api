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

package v1.services

import cats.data.EitherT
import cats.implicits._
import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.RetrievePeriodicObligationsConnector
import v1.controllers.EndpointLogContext
import v1.models.domain.business.MtdBusiness._
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrievePeriodObligations.RetrievePeriodicObligationsRequest
import v1.models.response.retrievePeriodObligations.RetrievePeriodObligationsResponse
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

class RetrievePeriodicObligationsService @Inject()(connector: RetrievePeriodicObligationsConnector)
  extends DesResponseMappingSupport with Logging {

  def retrieve(request: RetrievePeriodicObligationsRequest)(
              implicit hc: HeaderCarrier,
              ec: ExecutionContext,
              logContext: EndpointLogContext): Future[RetrievePeriodicObligationsServiceOutcome] = {
    val result = for {
      desResponseWrapper <- EitherT(connector.retrievePeriodicObligations(request)).leftMap(mapDesErrors(desErrorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](filterPeriodicValues(desResponseWrapper, request.typeOfBusiness, request.incomeSourceId))
    } yield mtdResponseWrapper
    result.value
  }

  private def desErrorMap =
    Map(
      "INVALID_IDTYPE" -> DownstreamError,
      "INVALID_IDNUMBER" -> NinoFormatError,
      "INVALID_STATUS" -> DownstreamError,
      "INVALID_REGIME" -> DownstreamError,
      "INVALID_DATE_FROM" -> FromDateFormatError,
      "INVALID_DATE_TO" -> ToDateFormatError,
      "INVALID_DATE_RANGE" -> RuleDateRangeInvalidError,
      "NOT_FOUND" -> NotFoundError,
      "NOT_FOUND_BPKEY" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}
