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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockRetrieveCrystallisationObligationsConnector
import v1.models.domain.status.{DesStatus, MtdStatus}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.ObligationsTaxYear
import v1.models.request.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsRequest
import v1.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse
import v1.models.response.retrieveCrystallisationObligations.des.{DesObligation, DesRetrieveCrystallisationObligationsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCrystallisationObligationsServiceSpec extends UnitSpec {

  private val nino = "AA123456A"
  private val fromDate = "2018-04-06"
  private val toDate = "2019-04-05"
  private val correlationId = "X-123"

  private val requestData = RetrieveCrystallisationObligationsRequest(Nino(nino), ObligationsTaxYear(fromDate, toDate))

  trait Test extends MockRetrieveCrystallisationObligationsConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveCrystallisationObligationsService(
      connector = mockRetrieveCrystallisationObligationsConnector
    )
  }

  "service" when {
    "service call successsful" must {
      "return mapped result" in new Test {
        val desResponseModel = DesRetrieveCrystallisationObligationsResponse(Seq(
          DesObligation("earlier", "then", "before now", DesStatus.F, Some("now"))
        ))
        val responseModel = RetrieveCrystallisationObligationsResponse("earlier", "then", "before now", MtdStatus.Fulfilled, Some("now"))

        MockRetrieveCrystallisationObligationsConnector.doConnectorThing(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, desResponseModel))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseModel))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrieveCrystallisationObligationsConnector.doConnectorThing(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input = Seq(
          ("INVALID_IDNUMBER", NinoFormatError),
          ("INVALID_IDTYPE", DownstreamError),
          ("INVALID_STATUS", DownstreamError),
          ("INVALID_REGIME", DownstreamError),
          ("INVALID_DATE_FROM", DownstreamError),
          ("INVALID_DATE_TO", DownstreamError),
          ("INVALID_DATE_RANGE", DownstreamError),
          ("NOT_FOUND_BPKEY", NotFoundError),
          ("INSOLVENT_TRADER", RuleInsolventTraderError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }

      "error when the connector returns an empty obligations list (JSON Reads filter out other obligations)" in new Test {
        val responseModel = DesRetrieveCrystallisationObligationsResponse(Seq())

        MockRetrieveCrystallisationObligationsConnector.doConnectorThing(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
    }
  }
}
