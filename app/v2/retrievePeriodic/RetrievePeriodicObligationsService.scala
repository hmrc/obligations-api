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

package v2.retrievePeriodic

import api.controllers.RequestContext
import api.models.domain.business.MtdBusiness
import api.models.domain.{BusinessId, PeriodKey}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceOutcome
import cats.data.EitherT
import cats.implicits._
import v2.connectors.RetrieveObligationsConnector
import v2.models.response.downstream.DownstreamObligations
import v2.retrievePeriodic.model.request.RetrievePeriodicObligationsRequest
import v2.retrievePeriodic.model.response.RetrievePeriodObligationsResponse
import v2.services.BaseService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrievePeriodicObligationsService @Inject()(connector: RetrieveObligationsConnector) extends BaseService {

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDTYPE"      -> InternalError,
      "INVALID_IDNUMBER"    -> NinoFormatError,
      "INVALID_STATUS"      -> InternalError,
      "INVALID_REGIME"      -> InternalError,
      "INVALID_DATE_FROM"   -> FromDateFormatError,
      "INVALID_DATE_TO"     -> ToDateFormatError,
      "INVALID_DATE_RANGE"  -> RuleDateRangeInvalidError,
      "INSOLVENT_TRADER"    -> RuleInsolventTraderError,
      "NOT_FOUND"           -> NotFoundError,
      "NOT_FOUND_BPKEY"     -> NotFoundError,
      "SERVER_ERROR"        -> InternalError,
      "SERVICE_UNAVAILABLE" -> InternalError
    )

  def retrieve(request: RetrievePeriodicObligationsRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrievePeriodObligationsResponse]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveObligations(request.nino, request.dateRange, request.status))
        .leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](extractMtdResponse(downstreamResponseWrapper, request.typeOfBusiness, request.businessId))
    } yield mtdResponseWrapper

    result.value
  }

  private def extractMtdResponse(responseWrapper: ResponseWrapper[DownstreamObligations],
                                   typeOfBusiness: Option[MtdBusiness],
                                   businessId: Option[BusinessId]) =
    toMtdBusinessObligations(responseWrapper.responseData, typeOfBusiness, businessId)(_.periodKey != PeriodKey.EOPS) match {
      case Nil => Left(ErrorWrapper(responseWrapper.correlationId, NoObligationsFoundError))
      case obs => Right(ResponseWrapper(responseWrapper.correlationId, RetrievePeriodObligationsResponse(obs)))
    }

}
