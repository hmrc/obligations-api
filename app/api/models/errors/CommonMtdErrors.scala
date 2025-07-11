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

package api.models.errors

import play.api.http.Status._

// Format Errors
object NinoFormatError       extends MtdError("FORMAT_NINO", "The provided NINO is invalid", BAD_REQUEST)
object TaxYearFormatError    extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid", BAD_REQUEST)
object BusinessIdFormatError extends MtdError("FORMAT_BUSINESS_ID", "The provided businessId is invalid", BAD_REQUEST)

object TransactionIdFormatError extends MtdError(code = "FORMAT_TRANSACTION_ID", message = "The transaction ID format is invalid", BAD_REQUEST)

object CountryCodeFormatError extends MtdError("FORMAT_COUNTRY_CODE", "The provided Country code is invalid", BAD_REQUEST)

object ValueFormatError extends MtdError("FORMAT_VALUE", "The value must be between 0 and 99999999999.99", BAD_REQUEST) {

  def forPathAndRange(path: String, min: String, max: String): MtdError =
    ValueFormatError.copy(paths = Some(Seq(path)), message = s"The value must be between $min and $max")

}

object StartDateFormatError extends MtdError("FORMAT_START_DATE", "The provided Start date is invalid", BAD_REQUEST)

object EndDateFormatError extends MtdError("FORMAT_END_DATE", "The provided End date is invalid", BAD_REQUEST)

object CalculationIdFormatError extends MtdError("FORMAT_CALCULATION_ID", "The provided calculation ID is invalid", BAD_REQUEST)

object FromDateFormatError       extends MtdError("FORMAT_FROM_DATE", "The provided fromDate is invalid", BAD_REQUEST)
object ToDateFormatError         extends MtdError("FORMAT_TO_DATE", "The provided toDate is invalid", BAD_REQUEST)
object StatusFormatError         extends MtdError("FORMAT_STATUS", "The provided status is invalid", BAD_REQUEST)
object TypeOfBusinessFormatError extends MtdError("FORMAT_TYPE_OF_BUSINESS", "The provided type of business is invalid", BAD_REQUEST)

// Parameter errors
object MissingTypeOfBusinessError
    extends MtdError("MISSING_TYPE_OF_BUSINESS", "The type of business query parameter must be provided when a businessId is supplied", BAD_REQUEST)

object MissingFromDateError      extends MtdError("MISSING_FROM_DATE", "The From date parameter is missing", BAD_REQUEST)
object MissingToDateError        extends MtdError("MISSING_TO_DATE", "The To date parameter is missing", BAD_REQUEST)
object ToDateBeforeFromDateError extends MtdError("RANGE_TO_DATE_BEFORE_FROM_DATE", "The To date must be after the From date", BAD_REQUEST)
object NoObligationsFoundError   extends MtdError("NO_OBLIGATIONS_FOUND", "No obligations found using this filter", NOT_FOUND)

// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleTaxYearRangeExceededError
    extends MtdError("RULE_TAX_YEAR_RANGE_EXCEEDED", "Tax year range exceeded. A tax year range of one year is required", BAD_REQUEST)

object RuleTaxYearRangeInvalidError extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "A tax year range of one year is required", BAD_REQUEST)
object RuleDateRangeInvalidError    extends MtdError("RULE_DATE_RANGE_INVALID", "The specified date range is invalid", BAD_REQUEST)

object RuleTaxYearNotEndedError extends MtdError("RULE_TAX_YEAR_NOT_ENDED", "The specified tax year has not yet ended", BAD_REQUEST)

object RuleInsolventTraderError
    extends MtdError("RULE_INSOLVENT_TRADER", "The remote endpoint has indicated that the Trader is insolvent", BAD_REQUEST)

object RuleFromDateNotSupportedError
    extends MtdError("RULE_FROM_DATE_NOT_SUPPORTED", "The specified from date is not supported as too early", BAD_REQUEST)

//Standard Errors
object NotFoundError           extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)
object InternalError           extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)
object BadRequestError         extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)
object BVRError                extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)
object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error", INTERNAL_SERVER_ERROR)

//Authorisation Errors
object ClientOrAgentNotAuthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client or agent is not authorised", FORBIDDEN) {
  def withStatus401: MtdError = copy(httpStatus = UNAUTHORIZED)
}

object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError(code = "ACCEPT_HEADER_INVALID", message = "The accept header is missing or invalid", NOT_ACCEPTABLE)
object UnsupportedVersionError  extends MtdError(code = "NOT_FOUND", message = "The requested resource could not be found", NOT_FOUND)

object InvalidBodyTypeError
    extends MtdError(code = "INVALID_BODY_TYPE", message = "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)

object InvalidTaxYearParameterError
    extends MtdError(code = "INVALID_TAX_YEAR_PARAMETER", message = "A tax year before 2023-24 was supplied", BAD_REQUEST)

object RuleEndBeforeStartDateError
    extends MtdError("RULE_END_DATE_BEFORE_START_DATE", "The supplied accounting period end date is before the start date", BAD_REQUEST)

object RuleCountryCodeError extends MtdError("RULE_COUNTRY_CODE", "The country code is not a valid ISO 3166-1 alpha-3 country code", BAD_REQUEST)

//Stub errors
object RuleIncorrectGovTestScenarioError
    extends MtdError("RULE_INCORRECT_GOV_TEST_SCENARIO", "The supplied Gov-Test-Scenario is not valid", BAD_REQUEST)
