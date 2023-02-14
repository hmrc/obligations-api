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

package v1.controllers

import api.controllers._
import api.services.{ AuditService, EnrolmentsAuthService, MtdIdLookupService }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import utils.{ IdGenerator, Logging }
import v1.controllers.requestParsers.RetrieveEOPSObligationsRequestParser
import v1.models.request.retrieveEOPSObligations.RetrieveEOPSObligationsRawData
import v1.services._

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveEOPSObligationsController @Inject()(val authService: EnrolmentsAuthService,
                                                  val lookupService: MtdIdLookupService,
                                                  parser: RetrieveEOPSObligationsRequestParser,
                                                  service: RetrieveEOPSObligationsService,
                                                  auditService: AuditService,
                                                  cc: ControllerComponents,
                                                  idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveEOPSObligationsController", endpointName = "handleRequest")

  def handleRequest(nino: String,
                    typeOfBusiness: Option[String],
                    businessId: Option[String],
                    fromDate: Option[String],
                    toDate: Option[String],
                    status: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = RetrieveEOPSObligationsRawData(nino, typeOfBusiness, businessId, fromDate, toDate, status)

      val requestHandler = RequestHandler
        .withParser(parser)
        .withService(service.retrieve)
        .withPlainJsonResult()
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "retrieveEOPSObligations",
          transactionName = "retrieve-eops-obligations",
          pathParams = Map("nino" -> nino),
          includeResponse = true
        ))

      requestHandler.handleRequest(rawData)
    }
}
