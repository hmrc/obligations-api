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
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.MtdStatus
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrievePeriodObligations.RetrievePeriodicObligationsRequest
import v1.models.response.common.{ Obligation, ObligationDetail }
import v1.models.response.retrievePeriodicObligations.RetrievePeriodObligationsResponse

import scala.concurrent.Future

class RetrievePeriodicObligationsConnectorSpec extends ConnectorSpec {

  private val validNino           = "AA123456A"
  private val validTypeOfBusiness = MtdBusiness.`foreign-property`
  private val validBusinessId     = "XAIS123456789012"
  private val validFromDate       = "2018-04-06"
  private val validToDate         = "2019-04-05"
  private val validStatus         = MtdStatus.Open

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrievePeriodicObligationsConnector = new RetrievePeriodicObligationsConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)]        = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)

  }

  "retrieve" should {
    "return a result" when {
      "a status is passed in and the downstream call is successful" in new Test {
        val request = RetrievePeriodicObligationsRequest(Nino(validNino),
                                                         Some(validTypeOfBusiness),
                                                         Some(validBusinessId),
                                                         Some(validFromDate),
                                                         Some(validToDate),
                                                         Some(validStatus))
        val outcome = Right(
          ResponseWrapper(
            correlationId,
            RetrievePeriodObligationsResponse(Seq(Obligation(
              typeOfBusiness = validTypeOfBusiness,
              businessId = validBusinessId,
              obligationDetails = Seq(ObligationDetail(validFromDate, validToDate, validToDate, Some(validFromDate), MtdStatus.Open))
            )))
          ))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/obligation-data/nino/$validNino/ITSA?from=$validFromDate&to=$validToDate&status=${validStatus.toDes}",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrievePeriodicObligations(request)) shouldBe outcome
      }
      "no status is passed in and the downstream call is successful" in new Test {
        val request = RetrievePeriodicObligationsRequest(Nino(validNino),
                                                         Some(validTypeOfBusiness),
                                                         Some(validBusinessId),
                                                         Some(validFromDate),
                                                         Some(validToDate),
                                                         None)
        val outcome = Right(
          ResponseWrapper(
            correlationId,
            RetrievePeriodObligationsResponse(Seq(Obligation(
              typeOfBusiness = validTypeOfBusiness,
              businessId = validBusinessId,
              obligationDetails = Seq(ObligationDetail(validFromDate, validToDate, validToDate, Some(validFromDate), MtdStatus.Open))
            )))
          ))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/obligation-data/nino/$validNino/ITSA?from=$validFromDate&to=$validToDate",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrievePeriodicObligations(request)) shouldBe outcome
      }
    }
  }
}
