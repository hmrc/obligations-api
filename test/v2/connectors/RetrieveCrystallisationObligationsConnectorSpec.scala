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

package v2.connectors

import api.connectors.ConnectorSpec
import api.models.domain.Nino
import api.models.domain.status.DesStatus.F
import api.models.outcomes.ResponseWrapper
import v2.models.request.ObligationsTaxYear
import v2.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest
import v2.models.response.retrieveCrystallisationObligations.des.{DesObligation, DesRetrieveCrystallisationObligationsResponse}

import scala.concurrent.Future

class RetrieveCrystallisationObligationsConnectorSpec extends ConnectorSpec {

  "RetrieveCrystallisationObligationsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new DesTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[DesRetrieveCrystallisationObligationsResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(s"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA?from=$fromDate&to=$toDate")
          .returns(Future.successful(outcome))

        await(connector.retrieveCrystallisationObligations(request)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val nino     = "AA123456A"
    protected val fromDate = "2018-04-06"
    protected val toDate   = "2019-04-05"

    val connector: RetrieveCrystallisationObligationsConnector =
      new RetrieveCrystallisationObligationsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    lazy val request: RetrieveCrystallisationObligationsRequest =
      RetrieveCrystallisationObligationsRequest(Nino(nino), ObligationsTaxYear(fromDate, toDate))
    lazy val response: DesRetrieveCrystallisationObligationsResponse =
      DesRetrieveCrystallisationObligationsResponse(
        Seq(
          DesObligation(
            inboundCorrespondenceFromDate = fromDate,
            inboundCorrespondenceToDate = toDate,
            inboundCorrespondenceDueDate = toDate,
            status = F,
            inboundCorrespondenceDateReceived = Some(toDate)
          )))
  }
}
