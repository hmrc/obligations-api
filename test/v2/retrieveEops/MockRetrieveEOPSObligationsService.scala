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

package v2.retrieveEops

import api.controllers.RequestContext
import api.models.errors.ErrorWrapper
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v2.retrieveEops.model.request.RetrieveEOPSObligationsRequest
import v2.retrieveEops.model.response.RetrieveEOPSObligationsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveEOPSObligationsService extends TestSuite with MockFactory {

  val mockService: RetrieveEOPSObligationsService = mock[RetrieveEOPSObligationsService]

  object MockRetrieveEOPSObligationsService {

    def retrieve(
        requestData: RetrieveEOPSObligationsRequest): CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[RetrieveEOPSObligationsResponse]]]] = {
      (mockService
        .retrieve(_: RetrieveEOPSObligationsRequest)(_: RequestContext, _: ExecutionContext))
        .expects(requestData, *, *)
    }

  }

}
