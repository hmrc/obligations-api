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

package v3.connectors

import api.connectors.ConnectorSpec
import api.models.domain.status.MtdStatusV3
import api.models.domain.{DateRange, Nino}
import api.models.outcomes.ResponseWrapper
import org.scalatest.TestSuite
import uk.gov.hmrc.http.StringContextOps
import v3.models.response.downstream.DownstreamObligations

import java.time.LocalDate
import scala.concurrent.Future

class RetrieveObligationsConnectorSpec extends TestSuite with ConnectorSpec {

  "RetrieveObligationsConnector" when {
    "all optional parameters are absent" should {
      "make the request with no query parameters " in new DesTest with Test {
        private val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(url"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA")
          .returns(Future.successful(outcome))

        await(connector.retrieveObligations(Nino(nino), dateRange = None, status = None)) shouldBe outcome
      }
    }

    "a date range is specified" should {
      "make the request to ISO-formatted from and to query parameters" in new DesTest with Test {
        val from                 = "2020-01-01"
        val to                   = "2021-01-01"
        val dateRange: DateRange = DateRange(LocalDate.parse(from), LocalDate.parse(to))

        private val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(url"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA", parameters = Seq("from" -> from, "to" -> to))
          .returns(Future.successful(outcome))

        await(connector.retrieveObligations(Nino(nino), Some(dateRange), status = None)) shouldBe outcome
      }
    }

    "a status is specified" should {
      "make the request with the downstream-formatted status query parameter" in new DesTest with Test {
        private val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(url"$baseUrl/enterprise/obligation-data/nino/$nino/ITSA", parameters = Seq("status" -> "O"))
          .returns(Future.successful(outcome))

        await(connector.retrieveObligations(Nino(nino), dateRange = None, Some(MtdStatusV3.open))) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    lazy val response: DownstreamObligations = DownstreamObligations(Nil)

    val connector = new RetrieveObligationsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    protected val nino = "AA123456A"
  }

}
