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

package v1.models.errors

import play.api.libs.json.{ Json, OWrites }

case class MtdError(code: String, message: String)

object MtdError {
  implicit val writes: OWrites[MtdError] = Json.writes[MtdError]

  implicit def genericWrites[T <: MtdError]: OWrites[T] =
    writes.contramap[T](c => c: MtdError)
}

object NinoFormatError           extends MtdError("FORMAT_NINO", "The provided NINO is invalid")
object TaxYearFormatError        extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid")
object BusinessIdFormatError     extends MtdError("FORMAT_BUSINESS_ID", "The provided businessId is invalid")
object FromDateFormatError       extends MtdError("FORMAT_FROM_DATE", "The provided fromDate is invalid")
object ToDateFormatError         extends MtdError("FORMAT_TO_DATE", "The provided toDate is invalid")
object StatusFormatError         extends MtdError("FORMAT_STATUS", "The provided status is invalid")
object TypeOfBusinessFormatError extends MtdError("FORMAT_TYPE_OF_BUSINESS", "The provided type of business is invalid")

object MissingTypeOfBusinessError
    extends MtdError("MISSING_TYPE_OF_BUSINESS", "The type of business query parameter must be provided when a businessId is supplied.")
object MissingFromDateError extends MtdError("MISSING_FROM_DATE", "The From date parameter is missing")
object MissingToDateError   extends MtdError("MISSING_TO_DATE", "The To date parameter is missing")

object ToDateBeforeFromDateError extends MtdError("RANGE_TO_DATE_BEFORE_FROM_DATE", "The To date must be after the From date")

object NoObligationsFoundError extends MtdError("NO_OBLIGATIONS_FOUND", "No obligations found using this filter")

// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "Tax year not supported, because it precedes the earliest allowable tax year")

object RuleIncorrectOrEmptyBodyError extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted")

object RuleTaxYearRangeExceededError
    extends MtdError("RULE_TAX_YEAR_RANGE_EXCEEDED", "Tax year range exceeded. A tax year range of one year is required")

object RuleDateRangeInvalidError extends MtdError("RULE_DATE_RANGE_INVALID", "The specified date range is invalid")

object RuleInsolventTraderError extends MtdError("RULE_INSOLVENT_TRADER", "The remote endpoint has indicated that the Trader is insolvent")

object RuleFromDateNotSupportedError extends MtdError("RULE_FROM_DATE_NOT_SUPPORTED", "The specified from date is not supported as too early")
//Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")

object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error")

object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")

//Authorisation Errors
object UnauthorisedError       extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised")
object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized")

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found")

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body")
