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
import api.models.domain.status.MtdStatus.Fulfilled
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.models.request.TaxYearRange
import api.services.{ ServiceOutcome, ServiceSpec }
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.RetrieveCrystallisationObligationsFixtures.{ desObligationModel, mtdObligationModel }
import v2.mocks.connectors.MockRetrieveCrystallisationObligationsConnector
import v2.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest
import v2.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse
import v2.models.response.retrieveCrystallisationObligations.des.DesRetrieveCrystallisationObligationsResponse

import scala.concurrent.Future

class RetrieveCrystallisationObligationsServiceSpec extends ServiceSpec {

  val downstreamResponseModel: DesRetrieveCrystallisationObligationsResponse = DesRetrieveCrystallisationObligationsResponse(
    Seq(desObligationModel()))

  val mtdResponseModel: RetrieveCrystallisationObligationsResponse =
    RetrieveCrystallisationObligationsResponse(Seq(mtdObligationModel()))
  private val nino   = "AA123456A"
  private val status = Fulfilled
  private val requestData =
    RetrieveCrystallisationObligationsRequest(Nino(nino), TaxYearRange.fromMtd("2018-19"), Some(status))

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

        val result: ServiceOutcome[RetrieveCrystallisationObligationsResponse] = await(service.retrieve(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, mtdResponseModel))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveCrystallisationObligationsConnector
              .retrieve(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: ServiceOutcome[RetrieveCrystallisationObligationsResponse] = await(service.retrieve(requestData))
            result shouldBe Left(ErrorWrapper(correlationId, error))
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

        val result: ServiceOutcome[RetrieveCrystallisationObligationsResponse] = await(service.retrieve(requestData))
        result shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
      }
    }
  }
}
