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

package v2.retrieveCrystallisation

import api.controllers.EndpointLogContext
import api.models.domain.business.DesBusiness
import api.models.domain.status.MtdStatus.Fulfilled
import api.models.domain.{DateRange, Nino, TaxYear, TaxYearRange}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.MockRetrieveObligationsConnector
import v2.models.response.domain.ObligationsFixture
import v2.models.response.downstream.{DownstreamObligations, DownstreamObligationsFixture}
import v2.retrieveCrystallisation.model.request.RetrieveCrystallisationObligationsRequest
import v2.retrieveCrystallisation.model.response.RetrieveCrystallisationObligationsResponse

import scala.concurrent.Future

class RetrieveCrystallisationObligationsServiceSpec extends ServiceSpec with DownstreamObligationsFixture with ObligationsFixture {

  private val nino   = "AA123456A"
  private val status = Fulfilled

  private val requestData =
    RetrieveCrystallisationObligationsRequest(
      Nino(nino),
      TaxYearRange(from = TaxYear.fromMtd("2018-19"), to = TaxYear.fromMtd("2020-21")),
      Some(status))

  private val taxYearRangeDateRange = DateRange.parse("2018-04-06", "2021-04-05")

  trait Test extends MockRetrieveObligationsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveCrystallisationObligationsService(
      connector = mockRetrieveObligationsConnector
    )

  }

  "service" when {
    "connector call is successful" when {
      "a single obligation is returned from downstream" must {
        "return the obligation" in new Test {
          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSA))),
                obligationDetails = Seq(downstreamObligationDetail())
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(Nino(nino), dateRange = Some(taxYearRangeDateRange), status = Some(status))
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResult))))

          private val result = RetrieveCrystallisationObligationsResponse(
            Seq(
              obligationDetail()
            ))

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "some obligations are not ITSA" must {
        "remove them" in new Test {
          // To label obligation details so we can tell them apart...
          val itsaDate    = "2001-01-01"
          val nonItsaDate = "2002-02-02"

          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification()),
                obligationDetails = Seq(downstreamObligationDetail(inboundCorrespondenceFromDate = nonItsaDate))
              ),
              downstreamObligation(
                identification = Some(downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSA))),
                obligationDetails = Seq(downstreamObligationDetail(inboundCorrespondenceFromDate = itsaDate))
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(Nino(nino), dateRange = Some(taxYearRangeDateRange), status = Some(status))
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResult))))

          private val result = RetrieveCrystallisationObligationsResponse(
            Seq(
              obligationDetail(periodStartDate = itsaDate)
            ))

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "multiple obligation details are present within a single ITSA obligation" must {
        "combine them" in new Test {
          val date1 = "2001-01-01"
          val date2 = "2002-02-02"

          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSA))),
                obligationDetails = Seq(
                  downstreamObligationDetail(inboundCorrespondenceFromDate = date1),
                  downstreamObligationDetail(inboundCorrespondenceFromDate = date2))
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(Nino(nino), dateRange = Some(taxYearRangeDateRange), status = Some(status))
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResult))))

          private val result = RetrieveCrystallisationObligationsResponse(
            Seq(
              obligationDetail(periodStartDate = date1),
              obligationDetail(periodStartDate = date2)
            ))

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "obligation details are present within a multiple ITSA obligations" must {
        "combine them" in new Test {
          val date1 = "2001-01-01"
          val date2 = "2002-02-02"

          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSA))),
                obligationDetails = Seq(downstreamObligationDetail(inboundCorrespondenceFromDate = date1))
              ),
              downstreamObligation(
                identification = Some(downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSA))),
                obligationDetails = Seq(downstreamObligationDetail(inboundCorrespondenceFromDate = date2))
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(Nino(nino), dateRange = Some(taxYearRangeDateRange), status = Some(status))
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResult))))

          private val result = RetrieveCrystallisationObligationsResponse(
            Seq(
              obligationDetail(periodStartDate = date1),
              obligationDetail(periodStartDate = date2)
            ))

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "no ITSA obligation details are present" must {
        "return a NoObligationsFoundError" in new Test {
          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification()),
                obligationDetails = Seq(downstreamObligationDetail())
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(Nino(nino), dateRange = Some(taxYearRangeDateRange), status = Some(status))
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResult))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
        }
      }

      "no obligations at all are present" must {
        "return a NoObligationsFoundError" in new Test {
          private val downstreamResult = DownstreamObligations(Nil)

          MockRetrieveObligationsConnector
            .retrieveObligations(Nino(nino), dateRange = Some(taxYearRangeDateRange), status = Some(status))
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResult))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
        }
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveObligationsConnector
              .retrieveObligations(Nino(nino), Some(taxYearRangeDateRange), Some(status))
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
    }
  }

}
