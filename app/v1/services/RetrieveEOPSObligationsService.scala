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

package v1.services

import api.controllers.RequestContext
import api.models.errors
import api.models.errors._
import api.services.BaseService
import cats.data.EitherT
import cats.implicits._
import v1.connectors.RetrieveEOPSObligationsConnector
import v1.models.request.retrieveEOPSObligations.RetrieveEOPSObligationsRequest

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class RetrieveEOPSObligationsService @Inject()(connector: RetrieveEOPSObligationsConnector) extends BaseService {

  def retrieve(request: RetrieveEOPSObligationsRequest)(implicit ctx: RequestContext,
                                                        ec: ExecutionContext): Future[RetrieveEOPSObligationsServiceOutcome] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveEOPSObligations(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper        <- EitherT.fromEither[Future](filterEOPSValues(downstreamResponseWrapper, request.typeOfBusiness, request.businessId))
    } yield mtdResponseWrapper

    result.value

  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDNUMBER"    -> NinoFormatError,
      "INVALID_IDTYPE"      -> errors.InternalError,
      "INVALID_STATUS"      -> errors.InternalError,
      "INVALID_REGIME"      -> errors.InternalError,
      "INVALID_DATE_FROM"   -> FromDateFormatError,
      "INVALID_DATE_TO"     -> ToDateFormatError,
      "INVALID_DATE_RANGE"  -> RuleDateRangeInvalidError,
      "INSOLVENT_TRADER"    -> RuleInsolventTraderError,
      "NOT_FOUND_BPKEY"     -> NotFoundError,
      "NOT_FOUND"           -> NotFoundError,
      "SERVER_ERROR"        -> errors.InternalError,
      "SERVICE_UNAVAILABLE" -> errors.InternalError
    )

}
