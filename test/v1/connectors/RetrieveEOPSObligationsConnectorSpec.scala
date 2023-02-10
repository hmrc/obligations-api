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

package v1.connectors

import api.connectors.ConnectorSpec
import api.models.domain.Nino
import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatus
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveEOPSObligations.RetrieveEOPSObligationsRequest
import v1.models.response.common.{ Obligation, ObligationDetail }
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse

import scala.concurrent.Future

class RetrieveEOPSObligationsConnectorSpec extends ConnectorSpec {

  "RetrieveEOPSObligationsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new DesTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveEOPSObligationsResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA?from=$fromDate&to=$toDate&status=${status.toDes}")
          .returns(Future.successful(outcome))

        await(connector.retrieveEOPSObligations(request)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val nino                        = "AA123456A"
    protected val typeOfBusiness: MtdBusiness = MtdBusiness.`foreign-property`
    protected val businessId                  = "XAIS123456789012"
    protected val fromDate                    = "2018-04-06"
    protected val toDate                      = "2019-04-05"
    protected val status: MtdStatus           = MtdStatus.Open

    val connector: RetrieveEOPSObligationsConnector = new RetrieveEOPSObligationsConnector(http = mockHttpClient, appConfig = mockAppConfig)
    lazy val request: RetrieveEOPSObligationsRequest =
      RetrieveEOPSObligationsRequest(Nino(nino), Some(typeOfBusiness), Some(businessId), Some(fromDate), Some(toDate), Some(status))
    lazy val response: RetrieveEOPSObligationsResponse = RetrieveEOPSObligationsResponse(
      Seq(
        Obligation(
          typeOfBusiness = typeOfBusiness,
          businessId = businessId,
          obligationDetails = Seq(ObligationDetail(fromDate, toDate, toDate, Some(fromDate), MtdStatus.Open))
        )))
  }
}
