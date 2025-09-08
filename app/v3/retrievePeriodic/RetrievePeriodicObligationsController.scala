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

package v3.retrievePeriodic

import api.controllers.*
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrievePeriodicObligationsController @Inject() (val authService: EnrolmentsAuthService,
                                                       val lookupService: MtdIdLookupService,
                                                       validatorFactory: RetrievePeriodicObligationsValidatorFactory,
                                                       service: RetrievePeriodicObligationsService,
                                                       auditService: AuditService,
                                                       cc: ControllerComponents,
                                                       idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc)
    with Logging {

  override val endpointName: String = "retrieve-periodic-obligations"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrievePeriodicObligationsController", endpointName = "handleRequest")

  def handleRequest(nino: String,
                    typeOfBusiness: Option[String],
                    businessId: Option[String],
                    fromDate: Option[String],
                    toDate: Option[String],
                    status: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, typeOfBusiness, businessId, fromDate, toDate, status)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.retrieve)
        .withPlainJsonResult()
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "retrievePeriodicObligations",
          transactionName = "retrieve-periodic-obligations",
          pathParams = Map("nino" -> nino),
          includeResponse = true
        ))

      requestHandler.handleRequest()
    }

}
