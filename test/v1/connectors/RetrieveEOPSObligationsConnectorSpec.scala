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
import api.models.domain.{DateRange, Nino}
import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatus
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveEOPSObligations.RetrieveEOPSObligationsRequest
import v1.models.response.common.{Obligation, ObligationDetail}
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse

import java.time.LocalDate
import scala.concurrent.Future

class RetrieveEOPSObligationsConnectorSpec extends ConnectorSpec {

  "RetrieveEOPSObligationsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new DesTest with Test {
        val request: RetrieveEOPSObligationsRequest =
          RetrieveEOPSObligationsRequest(Nino(nino), None, None, None, None)

        val outcome: Right[Nothing, ResponseWrapper[RetrieveEOPSObligationsResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA")
          .returns(Future.successful(outcome))

        await(connector.retrieveEOPSObligations(request)) shouldBe outcome
      }

      "a valid request is made when a date range is supplied" in new DesTest with Test {
        val from = "2020-01-01"
        val to   = "2021-01-01"

        val request: RetrieveEOPSObligationsRequest =
          RetrieveEOPSObligationsRequest(Nino(nino), None, None, Some(DateRange(LocalDate.parse(from), LocalDate.parse(to))), None)

        val outcome: Right[Nothing, ResponseWrapper[RetrieveEOPSObligationsResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA", parameters = Seq("from" -> from, "to" -> to))
          .returns(Future.successful(outcome))

        await(connector.retrieveEOPSObligations(request)) shouldBe outcome
      }

      "a valid request is made when a status is supplied" in new DesTest with Test {
        val request: RetrieveEOPSObligationsRequest =
          RetrieveEOPSObligationsRequest(Nino(nino), None, None, None, Some(MtdStatus.Open))

        val outcome: Right[Nothing, ResponseWrapper[RetrieveEOPSObligationsResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA", parameters = Seq("status" -> "O"))
          .returns(Future.successful(outcome))

        await(connector.retrieveEOPSObligations(request)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    lazy val response: RetrieveEOPSObligationsResponse = RetrieveEOPSObligationsResponse(
      Seq(
        Obligation(
          typeOfBusiness = MtdBusiness.`foreign-property`,
          businessId = "XAIS123456789012",
          obligationDetails = Seq(ObligationDetail("2018-04-06", "2019-04-05", "2019-04-05", Some("2018-04-06"), MtdStatus.Open))
        )))

    val connector: RetrieveEOPSObligationsConnector = new RetrieveEOPSObligationsConnector(http = mockHttpClient, appConfig = mockAppConfig)
    protected val nino                              = "AA123456A"
  }

}
