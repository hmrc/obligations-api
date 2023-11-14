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
import v1.models.request.retrievePeriodObligations.RetrievePeriodicObligationsRequest
import v1.models.response.common.{Obligation, ObligationDetail}
import v1.models.response.retrievePeriodicObligations.RetrievePeriodObligationsResponse

import java.time.LocalDate
import scala.concurrent.Future

class RetrievePeriodicObligationsConnectorSpec extends ConnectorSpec {

  "RetrievePeriodicObligationsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made when no query parameters are supplied" in new DesTest with Test {
        val request: RetrievePeriodicObligationsRequest = RetrievePeriodicObligationsRequest(Nino(nino), None, None, None, None)

        val outcome: Right[Nothing, ResponseWrapper[RetrievePeriodObligationsResponse]] = Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA")
          .returns(Future.successful(outcome))

        await(connector.retrievePeriodicObligations(request)) shouldBe outcome
      }

      "a valid request is made when a date range is supplied" in new DesTest with Test {
        val from = "2020-01-01"
        val to   = "2021-01-01"

        val request: RetrievePeriodicObligationsRequest =
          RetrievePeriodicObligationsRequest(Nino(nino), None, None, Some(DateRange(LocalDate.parse(from), LocalDate.parse(to))), None)

        val outcome: Right[Nothing, ResponseWrapper[RetrievePeriodObligationsResponse]] = Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA", parameters = Seq("from" -> from, "to" -> to))
          .returns(Future.successful(outcome))

        await(connector.retrievePeriodicObligations(request)) shouldBe outcome
      }

      "a valid request is made when a status is supplied" in new DesTest with Test {
        val request: RetrievePeriodicObligationsRequest =
          RetrievePeriodicObligationsRequest(Nino(nino), None, None, None, Some(MtdStatus.Open))

        val outcome: Right[Nothing, ResponseWrapper[RetrievePeriodObligationsResponse]] = Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA", parameters = Seq("status" -> "O"))
          .returns(Future.successful(outcome))

        await(connector.retrievePeriodicObligations(request)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    lazy val response: RetrievePeriodObligationsResponse = RetrievePeriodObligationsResponse(
      Seq(
        Obligation(
          typeOfBusiness = MtdBusiness.`foreign-property`,
          businessId = "XAIS123456789012",
          obligationDetails = Seq(ObligationDetail("2018-04-06", "2019-04-05", "2019-04-05", Some("2018-04-06"), MtdStatus.Open))
        )))

    val connector: RetrievePeriodicObligationsConnector =
      new RetrievePeriodicObligationsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    protected val nino = "AA123456A"
  }

}
