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

import api.controllers.RequestContext
import api.models.errors.ErrorWrapper
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v3.retrievePeriodic.model.request.RetrievePeriodicObligationsRequest
import v3.retrievePeriodic.model.response.RetrievePeriodObligationsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrievePeriodicObligationsService extends TestSuite with MockFactory {

  val mockService: RetrievePeriodicObligationsService = mock[RetrievePeriodicObligationsService]

  object MockRetrievePeriodicObligationsService {

    def retrieve(requestData: RetrievePeriodicObligationsRequest)
        : CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[RetrievePeriodObligationsResponse]]]] = {
      (mockService
        .retrieve(_: RetrievePeriodicObligationsRequest)(_: RequestContext, _: ExecutionContext))
        .expects(requestData, *, *)
    }

  }

}
