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

package v2.services

import api.controllers.EndpointLogContext
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.RetrieveCrystallisationObligationsFixtures.{ desObligationModel, mtdObligationModel }
import v2.mocks.connectors.MockRetrieveCrystallisationObligationsConnector
import v2.models.request.ObligationsTaxYear
import v2.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest
import v2.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse
import v2.models.response.retrieveCrystallisationObligations.des.DesRetrieveCrystallisationObligationsResponse

import scala.concurrent.Future

class RetrieveCrystallisationObligationsServiceSpec extends ServiceSpec {

  private val nino     = "AA123456A"
  private val fromDate = "2018-04-06"
  private val toDate   = "2019-04-05"

  private val requestData = RetrieveCrystallisationObligationsRequest(Nino(nino), ObligationsTaxYear(fromDate, toDate))

  val downstreamResponseModel: DesRetrieveCrystallisationObligationsResponse = DesRetrieveCrystallisationObligationsResponse(
    Seq(desObligationModel()))

  val mtdResponseModel: RetrieveCrystallisationObligationsResponse =
    RetrieveCrystallisationObligationsResponse(Seq(mtdObligationModel()))

  trait Test extends MockRetrieveCrystallisationObligationsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveCrystallisationObligationsService(
      connector = mockRetrieveCrystallisationObligationsConnector
    )
  }

  "service" should {
    "return a successful response" when {
      "a successful response is pased through" in new Test {

        MockRetrieveCrystallisationObligationsConnector
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResponseModel))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, mtdResponseModel))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveCrystallisationObligationsConnector
              .retrieve(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = List(
          ("INVALID_IDNUMBER", NinoFormatError),
          ("INVALID_IDTYPE", InternalError),
          ("INVALID_STATUS", InternalError),
          ("INVALID_REGIME", InternalError),
          ("INVALID_DATE_FROM", InternalError),
          ("INVALID_DATE_TO", InternalError),
          ("INVALID_DATE_RANGE", InternalError),
          ("NOT_FOUND_BPKEY", NotFoundError),
          ("INSOLVENT_TRADER", RuleInsolventTraderError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }

      "error when the connector returns an empty obligations list (JSON Reads filter out other obligations)" in new Test {
        val responseModel: DesRetrieveCrystallisationObligationsResponse = DesRetrieveCrystallisationObligationsResponse(Seq())

        MockRetrieveCrystallisationObligationsConnector
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
      }
    }
  }
}
