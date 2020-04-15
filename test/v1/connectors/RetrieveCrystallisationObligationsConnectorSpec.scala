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

package v1.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.domain.status.MtdStatus.Fulfilled
import v1.models.outcomes.ResponseWrapper
import v1.models.request.ObligationsTaxYear
import v1.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest
import v1.models.response.retrieveCrystallisationObligations.{Obligation, RetrieveCrystallisationObligationsResponse}

import scala.concurrent.Future

class RetrieveCrystallisationObligationsConnectorSpec extends ConnectorSpec {

  private val validNino = Nino("AA123456A")
  private val fromDate = "2018-04-06"
  private val toDate = "2019-04-05"

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrieveCrystallisationObligationsConnector = new RetrieveCrystallisationObligationsConnector(
      http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "retrieve" should {
    val request = RetrieveCrystallisationObligationsRequest(validNino, ObligationsTaxYear(fromDate, toDate))

    "return a result" when {
      "the downstream call is successful and a source is passed in" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, RetrieveCrystallisationObligationsResponse(
          Seq(Obligation(fromDate, toDate, toDate, Fulfilled, Some(toDate)))))
        )
        MockedHttpClient.
          get(
            url = s"$baseUrl/enterprise/obligation-data/nino/${request.nino}/ITSA?from=${request.obligationsTaxYear.from}&to=${request.obligationsTaxYear.to}",
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          ).returns(Future.successful(outcome))
        await(connector.retrieveCrystallisationObligations(request)) shouldBe outcome
      }
    }
  }
}
