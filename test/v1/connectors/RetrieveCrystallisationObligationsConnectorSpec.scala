/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.domain.status.DesStatus.F
import v1.models.outcomes.ResponseWrapper
import v1.models.request.ObligationsTaxYear
import v1.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest
import v1.models.response.retrieveCrystallisationObligations.des.{ DesObligation, DesRetrieveCrystallisationObligationsResponse }

import scala.concurrent.Future

class RetrieveCrystallisationObligationsConnectorSpec extends ConnectorSpec {

  private val validNino = "AA123456A"
  private val fromDate  = "2018-04-06"
  private val toDate    = "2019-04-05"

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveCrystallisationObligationsConnector =
      new RetrieveCrystallisationObligationsConnector(http = mockHttpClient, appConfig = mockAppConfig)
    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "retrieve" should {
    val request = RetrieveCrystallisationObligationsRequest(Nino(validNino), ObligationsTaxYear(fromDate, toDate))

    "return a result" when {
      "the downstream call is successful and a source is passed in" in new Test {
        val outcome = Right(
          ResponseWrapper(
            correlationId,
            DesRetrieveCrystallisationObligationsResponse(
              Seq(DesObligation(
                inboundCorrespondenceFromDate = fromDate,
                inboundCorrespondenceToDate = toDate,
                inboundCorrespondenceDueDate = toDate,
                status = F,
                inboundCorrespondenceDateReceived = Some(toDate)
              )))
          )
        )

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/obligation-data/nino/$validNino/ITSA?from=$fromDate&to=$toDate",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))
        await(connector.retrieveCrystallisationObligations(request)) shouldBe outcome
      }
    }
  }
}
