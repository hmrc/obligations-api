/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockRetrieveEOPSObligationsConnector
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.MtdStatus
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveEOPSObligations.RetrieveEOPSObligationsRequest
import v1.models.response.common.{Obligation, ObligationDetail}
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveEOPSObligationsServiceSpec extends UnitSpec{

  private val nino = "AA123456A"
  private val typeOfBusiness = MtdBusiness.`self-employment`
  private val incomeSourceId = "XAIS123456789012"
  private val fromDate = "2018-04-06"
  private val toDate = "2019-04-05"
  private val status = MtdStatus.Open
  private val correlationId = "X-123"
  private val detail = ObligationDetail(fromDate, toDate, "2018-04-06", Some("2019-12-15"), status)


  private val requestData = RetrieveEOPSObligationsRequest(Nino(nino), Some(typeOfBusiness), Some(incomeSourceId), Some(fromDate), Some(toDate), Some(status))

  trait Test extends MockRetrieveEOPSObligationsConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveEOPSObligationsService(
      connector = mockRetrieveEOPSObligationsConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return mapped result" in new Test {
        val responseModel = RetrieveEOPSObligationsResponse(Seq(
          Obligation(typeOfBusiness, "businessID", Seq(detail))
        ))

        MockRetrieveEOPSObligationsConnector.doConnectorThing(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseModel))
      }
    }
  }
}