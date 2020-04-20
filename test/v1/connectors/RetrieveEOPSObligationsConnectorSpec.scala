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
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.MtdStatus
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveEOPSObligations.RetrieveEOPSObligationsRequest
import v1.models.response.common.{Obligation, ObligationDetail}
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse

import scala.concurrent.Future

class RetrieveEOPSObligationsConnectorSpec extends ConnectorSpec {

  private val validNino = Nino("AA123456A")
  private val validtypeOfBusiness = MtdBusiness.`foreign-property`
  private val validincomeSourceId = "XAIS123456789012"
  private val validfromDate = "2018-04-06"
  private val validtoDate = "2019-04-05"
  private val validStatus = MtdStatus.Open

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrieveEOPSObligationsConnector = new RetrieveEOPSObligationsConnector(
      http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "retrieve" should {
    "return a result" when {
      "a status is passed in and the downstream call is successful" in new Test {
        val request = RetrieveEOPSObligationsRequest(validNino,
          Some(validtypeOfBusiness),
          Some(validincomeSourceId),
          Some(validfromDate),
          Some(validtoDate),
          Some(validStatus))
        val outcome = Right(ResponseWrapper(correlationId, RetrieveEOPSObligationsResponse(Seq(Obligation(
          validtypeOfBusiness, validincomeSourceId, Seq(ObligationDetail(validfromDate, validtoDate, validtoDate, Some(validfromDate), MtdStatus.Open)))))))

        MockedHttpClient.get(
          url = s"$baseUrl/enterprise/obligation-data/nino/${request.nino}/ITSA?from=${request.fromDate.get}&to=${request.toDate.get}&status=${request.status.get.toDes}",
          requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
        ).returns(Future.successful(outcome))
        await(connector.retrieveEOPSObligations(request)) shouldBe outcome
      }
      "no status is passed in and the downstream call is successful" in new Test {
        val request = RetrieveEOPSObligationsRequest(validNino,
          Some(validtypeOfBusiness),
          Some(validincomeSourceId),
          Some(validfromDate),
          Some(validtoDate),
          None)
        val outcome = Right(ResponseWrapper(correlationId, RetrieveEOPSObligationsResponse(Seq(Obligation(
          validtypeOfBusiness, validincomeSourceId, Seq(ObligationDetail(validfromDate, validtoDate, validtoDate, Some(validfromDate), MtdStatus.Open)))))))

        MockedHttpClient.
          get(
            url = s"$baseUrl/enterprise/obligation-data/nino/${request.nino}/ITSA?from=${request.fromDate.get}&to=${request.toDate.get}",
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          ).returns(Future.successful(outcome))
        await(connector.retrieveEOPSObligations(request)) shouldBe outcome
      }
    }
  }
}
