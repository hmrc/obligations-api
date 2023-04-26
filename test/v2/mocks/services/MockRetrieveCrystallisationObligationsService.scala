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

package v2.mocks.services

import api.controllers.RequestContext
import api.models.errors.ErrorWrapper
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v2.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest
import v2.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse
import v2.services.RetrieveCrystallisationObligationsService

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveCrystallisationObligationsService extends MockFactory {

  val mockRetrieveCrystallisationObligationsService: RetrieveCrystallisationObligationsService = mock[RetrieveCrystallisationObligationsService]

  object MockRetrieveCrystallisationObligationsService {

    def retrieve(requestData: RetrieveCrystallisationObligationsRequest)
      : CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[RetrieveCrystallisationObligationsResponse]]]] = {
      (mockRetrieveCrystallisationObligationsService
        .retrieve(_: RetrieveCrystallisationObligationsRequest)(_: RequestContext, _: ExecutionContext))
        .expects(requestData, *, *)
    }
  }

}
