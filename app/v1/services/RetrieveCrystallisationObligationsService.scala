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
import api.models.domain.DateRange
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceOutcome
import cats.data.EitherT
import v1.connectors.RetrieveObligationsConnector
import v1.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest
import v1.models.response.common.ITSAObligation
import v1.models.response.downstream.DownstreamObligations
import v1.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCrystallisationObligationsService @Inject() (connector: RetrieveObligationsConnector) extends BaseService {

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDNUMBER"    -> NinoFormatError,
      "INVALID_IDTYPE"      -> InternalError,
      "INVALID_STATUS"      -> InternalError,
      "INVALID_REGIME"      -> InternalError,
      "INVALID_DATE_FROM"   -> InternalError,
      "INVALID_DATE_TO"     -> InternalError,
      "INVALID_DATE_RANGE"  -> InternalError,
      "NOT_FOUND_BPKEY"     -> NotFoundError,
      "INSOLVENT_TRADER"    -> RuleInsolventTraderError,
      "NOT_FOUND"           -> NotFoundError,
      "SERVER_ERROR"        -> InternalError,
      "SERVICE_UNAVAILABLE" -> InternalError
    )

  def retrieve(request: RetrieveCrystallisationObligationsRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveCrystallisationObligationsResponse]] = {

    val dateRange = DateRange.parse(request.obligationsTaxYear.from, request.obligationsTaxYear.to)

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveObligations(request.nino, Some(dateRange), status = None))
        .leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](filterCrystallisationValues(downstreamResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def filterCrystallisationValues(
      responseWrapper: ResponseWrapper[DownstreamObligations]
  ): Either[ErrorWrapper, ResponseWrapper[RetrieveCrystallisationObligationsResponse]] = {

    val downstreamObligations = responseWrapper.responseData.obligations

    val obDetails = for {
      dsOb     <- downstreamObligations
      mtdOb    <- ITSAObligation.fromDownstream(dsOb).toSeq
      obDetail <- mtdOb.obligationDetails
    } yield obDetail

    obDetails match {
      case Seq(obligationDetail) =>
        import obligationDetail._

        val response = RetrieveCrystallisationObligationsResponse(
          periodStartDate = periodStartDate,
          periodEndDate = periodEndDate,
          dueDate = dueDate,
          status = status,
          receivedDate = receivedDate
        )

        Right(ResponseWrapper(responseWrapper.correlationId, response))

      case Nil => Left(ErrorWrapper(responseWrapper.correlationId, NoObligationsFoundError))
      case _   => Left(ErrorWrapper(responseWrapper.correlationId, InternalError))
    }

  }

}
