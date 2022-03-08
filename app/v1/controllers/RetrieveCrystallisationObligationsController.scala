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
import v1.controllers.requestParsers.RetrieveCrystallisationObligationsRequestParser
import v1.models.audit.{AuditEvent, AuditResponse, RetrieveCrystallisationObligationsAuditDetail}
import v1.models.errors._
import v1.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRawData
import v1.services.{RetrieveCrystallisationObligationsService, _}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCrystallisationObligationsController @Inject()(val authService: EnrolmentsAuthService,
                                                             val lookupService: MtdIdLookupService,
                                                             requestParser: RetrieveCrystallisationObligationsRequestParser,
                                                             service: RetrieveCrystallisationObligationsService,
                                                             auditService: AuditService,
                                                             cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveCrystallisationObligationsController", endpointName = "handleRequest")

  def handleRequest(nino: String, taxYear: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      val rawData = RetrieveCrystallisationObligationsRawData(nino, taxYear)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.retrieve(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(serviceResponse.responseData)

          auditSubmission(RetrieveCrystallisationObligationsAuditDetail(request.userDetails, nino, taxYear,
            serviceResponse.correlationId, AuditResponse(OK, Right(Some(response)))))

          Ok(response)
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)

        auditSubmission(RetrieveCrystallisationObligationsAuditDetail(request.userDetails, nino, taxYear,
          correlationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case NinoFormatError | TaxYearFormatError | RuleTaxYearNotSupportedError
           | RuleTaxYearRangeExceededError | BadRequestError => BadRequest(Json.toJson(errorWrapper))
      case RuleInsolventTraderError                          => Forbidden(Json.toJson(errorWrapper))
      case NotFoundError | NoObligationsFoundError           => NotFound(Json.toJson(errorWrapper))
      case DownstreamError                                   => InternalServerError(Json.toJson(errorWrapper))
      case _                                                 => unhandledError(errorWrapper)
    }
  }

  private def auditSubmission(details: RetrieveCrystallisationObligationsAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext) = {
    val event = AuditEvent("retrieveCrystallisationObligations", "retrieve-crystallisation-obligations", details)
    auditService.auditEvent(event)
  }

}
