/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.support

import utils.Logging
import v1.controllers.EndpointLogContext
import v1.models.domain.business.MtdBusiness
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse
import v1.models.response.retrieveCrystallisationObligations.des.DesRetrieveCrystallisationObligationsResponse
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse
import v1.models.response.retrievePeriodicObligations.RetrievePeriodObligationsResponse

trait DesResponseMappingSupport {
  self: Logging =>

  final def filterPeriodicValues(
                                  responseWrapper: ResponseWrapper[RetrievePeriodObligationsResponse],
                                  typeOfBusiness: Option[MtdBusiness],
                                  businessId: Option[String]
                                ): Either[ErrorWrapper, ResponseWrapper[RetrievePeriodObligationsResponse]] = {
    val filteredObligations = responseWrapper.responseData.obligations.filter {
      obligation => typeOfBusiness.forall(_ == obligation.typeOfBusiness)
    }.filter {
      obligation => businessId.forall(_ == obligation.businessId)
    }.filter {
      obligation => obligation.obligationDetails.nonEmpty
    }

    if (filteredObligations.nonEmpty) {
      Right(ResponseWrapper(responseWrapper.correlationId, RetrievePeriodObligationsResponse(
        filteredObligations
      )))
    } else {
      Left(ErrorWrapper(Some(responseWrapper.correlationId), NoObligationsFoundError))
    }
  }

  final def filterEOPSValues(
                              responseWrapper: ResponseWrapper[RetrieveEOPSObligationsResponse],
                              typeOfBusiness: Option[MtdBusiness],
                              businessId: Option[String]
                                ): Either[ErrorWrapper, ResponseWrapper[RetrieveEOPSObligationsResponse]] = {

    val filteredObligations = responseWrapper.responseData.obligations.filter {
      obligation => typeOfBusiness.forall(_ == obligation.typeOfBusiness)
    }.filter {
      obligation => businessId.forall(_ == obligation.businessId)
    }.filter {
      obligation => obligation.obligationDetails.nonEmpty
    }

    if (filteredObligations.nonEmpty) {
      Right(ResponseWrapper(responseWrapper.correlationId, RetrieveEOPSObligationsResponse(
        filteredObligations
      )))
    } else {
      Left(ErrorWrapper(Some(responseWrapper.correlationId), NoObligationsFoundError))
    }
  }

  final def filterCrystallisationValues(
                                         responseWrapper: ResponseWrapper[DesRetrieveCrystallisationObligationsResponse]
                                       ): Either[ErrorWrapper, ResponseWrapper[RetrieveCrystallisationObligationsResponse]] = {
    responseWrapper.responseData.obligationDetails.toList match {
      case desO :: Nil =>
        Right(ResponseWrapper(responseWrapper.correlationId, desO.toMtd))
      case Nil => Left(ErrorWrapper(Some(responseWrapper.correlationId), NoObligationsFoundError))
      case _ :: _ => Left(ErrorWrapper(Some(responseWrapper.correlationId), DownstreamError))
    }
  }

  final def mapDesErrors[D](errorCodeMap: PartialFunction[String, MtdError])(desResponseWrapper: ResponseWrapper[DesError])(
    implicit logContext: EndpointLogContext): ErrorWrapper = {

    lazy val defaultErrorCodeMapping: String => MtdError = { code =>
      logger.warn(s"[${logContext.controllerName}] [${logContext.endpointName}] - No mapping found for error code $code")
      DownstreamError
    }

    desResponseWrapper match {
      case ResponseWrapper(correlationId, DesErrors(error :: Nil)) =>
        ErrorWrapper(Some(correlationId), errorCodeMap.applyOrElse(error.code, defaultErrorCodeMapping), None)

      case ResponseWrapper(correlationId, DesErrors(errorCodes)) =>
        val mtdErrors = errorCodes.map(error => errorCodeMap.applyOrElse(error.code, defaultErrorCodeMapping))

        if (mtdErrors.contains(DownstreamError)) {
          logger.warn(
            s"[${logContext.controllerName}] [${logContext.endpointName}] [CorrelationId - $correlationId]" +
              s" - downstream returned ${errorCodes.map(_.code).mkString(",")}. Revert to ISE")
          ErrorWrapper(Some(correlationId), DownstreamError, None)
        } else {
          ErrorWrapper(Some(correlationId), BadRequestError, Some(mtdErrors))
        }

      case ResponseWrapper(correlationId, OutboundError(error, errors)) =>
        ErrorWrapper(Some(correlationId), error, errors)
    }
  }
}
