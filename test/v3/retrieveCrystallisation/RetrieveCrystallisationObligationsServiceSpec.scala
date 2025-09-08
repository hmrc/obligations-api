/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.retrieveCrystallisation

import api.controllers.EndpointLogContext
import api.models.domain.business.DesBusiness
import api.models.domain.status.MtdStatusV3
import api.models.domain.{DateRange, Nino, TaxYear, TaxYearRange}
import api.models.errors.{
  DownstreamErrorCode,
  DownstreamErrors,
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  NoObligationsFoundError,
  NotFoundError,
  RuleInsolventTraderError
}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v3.connectors.MockRetrieveObligationsConnector
import v3.models.response.domain.ObligationsFixture
import v3.models.response.downstream.{DownstreamObligations, DownstreamObligationsFixture}
import v3.retrieveCrystallisation.model.request.RetrieveCrystallisationObligationsRequest
import v3.retrieveCrystallisation.model.response.RetrieveCrystallisationObligationsResponse

import scala.concurrent.Future

class RetrieveCrystallisationObligationsServiceSpec extends ServiceSpec with DownstreamObligationsFixture with ObligationsFixture {

  private val nino                      = "AA123456A"
  private val taxYearRangeStart: String = "2017-18"
  private val taxYearRangeEnd: String   = "2019-20"
  private val status                    = MtdStatusV3.open
  private val taxYearRange              = TaxYearRange(TaxYear.fromMtd(taxYearRangeStart), TaxYear.fromMtd(taxYearRangeEnd))
  private val dateRange                 = DateRange(taxYearRange.from.startDate, taxYearRange.to.endDate)

  def request(nino: Nino,
              obligationsTaxYear: TaxYearRange = taxYearRange,
              status: Option[MtdStatusV3] = None): RetrieveCrystallisationObligationsRequest =
    RetrieveCrystallisationObligationsRequest(
      nino = nino,
      obligationsTaxYear = obligationsTaxYear,
      status = status
    )

  "service" when {

    "connector call is successful" when {

      "a Nino, TaxYearRange and status are provided" must {

        "pass these to the connector and return the expected result" in new Test {

          private val requestData = request(Nino(nino), taxYearRange, Some(status))

          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(Some(DesBusiness.ITSA))),
                obligationDetails = Seq(
                  downstreamObligationDetail(inboundCorrespondenceFromDate = taxYearRange.from.startDate.toString),
                  downstreamObligationDetail(inboundCorrespondenceFromDate = taxYearRange.to.endDate.toString)
                )
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, Some(dateRange), requestData.status)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResult))))

          private val result = RetrieveCrystallisationObligationsResponse(
            Seq(
              obligationDetail(periodStartDate = taxYearRange.from.startDate.toString),
              obligationDetail(periodStartDate = taxYearRange.to.endDate.toString)
            )
          )

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "a Nino and TaxYearRange, but no status are provided" must {

        "pass these to the connector and return the expected result" in new Test {

          private val requestData = request(Nino(nino), taxYearRange)

          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(Some(DesBusiness.ITSA))),
                obligationDetails = Seq(
                  downstreamObligationDetail(inboundCorrespondenceFromDate = taxYearRange.from.startDate.toString),
                  downstreamObligationDetail(inboundCorrespondenceFromDate = taxYearRange.to.endDate.toString)
                )
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, Some(dateRange), requestData.status)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResult))))

          private val result = RetrieveCrystallisationObligationsResponse(
            Seq(
              obligationDetail(periodStartDate = taxYearRange.from.startDate.toString),
              obligationDetail(periodStartDate = taxYearRange.to.endDate.toString)
            )
          )

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "obligation details are empty for an obligation" must {

        "remove that obligation" in new Test {
          private val requestData = request(Nino(nino), taxYearRange)

          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(Some(DesBusiness.ITSA))),
                obligationDetails = Seq()
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, Some(dateRange), requestData.status)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResult))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
        }
      }

      "the connector call returns an empty obligations" must {
        "return NoObligationsFoundError" in new Test {
          private val requestData = request(Nino(nino), taxYearRange)

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, Some(dateRange), None)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, DownstreamObligations(Nil)))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
        }
      }
    }

    "connector call is unsuccessful" must {
      "map errors according to spec" when {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            private val requestData = request(Nino(nino), taxYearRange, Some(status))

            MockRetrieveObligationsConnector
              .retrieveObligations(requestData.nino, Some(dateRange), requestData.status)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

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
          ("INSOLVENT_TRADER", RuleInsolventTraderError),
          ("NOT_FOUND_BPKEY", NotFoundError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )
        input.foreach(serviceError.tupled)
      }
    }
  }

  trait Test extends MockRetrieveObligationsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveCrystallisationObligationsService(
      connector = mockRetrieveObligationsConnector
    )

  }

}
