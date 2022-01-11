/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.controllers

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.controllers.requestParsers.RetrievePeriodicObligationsRequestParser
import v1.models.audit.{AuditEvent, AuditResponse, RetrievePeriodicObligationsAuditDetail}
import v1.models.errors._
import v1.models.request.retrievePeriodObligations.RetrievePeriodicObligationsRawData
import v1.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService, RetrievePeriodicObligationsService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrievePeriodicObligationsController @Inject()(val authService: EnrolmentsAuthService,
                                                      val lookupService: MtdIdLookupService,
                                                      requestParser: RetrievePeriodicObligationsRequestParser,
                                                      service: RetrievePeriodicObligationsService,
                                                      auditService: AuditService,
                                                      cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrievePeriodicObligationsController", endpointName = "handleRequest")

  def handleRequest(nino: String,
                    typeOfBusiness: Option[String],
                    businessId: Option[String],
                    fromDate: Option[String],
                    toDate: Option[String],
                    status: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      val rawData = RetrievePeriodicObligationsRawData(nino, typeOfBusiness, businessId, fromDate, toDate, status)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.retrieve(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(serviceResponse.responseData)

          auditSubmission(RetrievePeriodicObligationsAuditDetail(request.userDetails, nino, typeOfBusiness, businessId, fromDate, toDate, status,
            serviceResponse.correlationId, AuditResponse(OK, Right(Some(response)))))

          Ok(response)
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)

        auditSubmission(RetrievePeriodicObligationsAuditDetail(request.userDetails, nino, typeOfBusiness, businessId, fromDate, toDate, status,
          correlationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case NinoFormatError | TypeOfBusinessFormatError | BusinessIdFormatError
           | FromDateFormatError | ToDateFormatError | StatusFormatError
           | MissingFromDateError | MissingToDateError | ToDateBeforeFromDateError
           | MissingTypeOfBusinessError | RuleDateRangeInvalidError | RuleFromDateNotSupportedError
           | BadRequestError                       => BadRequest(Json.toJson(errorWrapper))
      case RuleInsolventTraderError                => Forbidden(Json.toJson(errorWrapper))
      case NotFoundError | NoObligationsFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError                         => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: RetrievePeriodicObligationsAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext) = {
    val event = AuditEvent("retrievePeriodicObligations", "retrieve-periodic-obligations", details)
    auditService.auditEvent(event)
  }

}
