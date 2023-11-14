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

package v1.services

import api.controllers.EndpointLogContext
import api.models.domain.{BusinessId, DateRange, Nino}
import api.models.domain.business.MtdBusiness
import api.models.domain.status.MtdStatus
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.connectors.MockRetrieveEOPSObligationsConnector
import v1.models.request.retrieveEOPSObligations.RetrieveEOPSObligationsRequest
import v1.models.response.common.{Obligation, ObligationDetail}
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse

import java.time.LocalDate
import scala.concurrent.Future

class RetrieveEOPSObligationsServiceSpec extends ServiceSpec {

  private val nino           = "AA123456A"
  private val typeOfBusiness = MtdBusiness.`self-employment`
  private val businessId     = "XAIS123456789012"
  private val fromDate       = "2018-04-06"
  private val toDate         = "2019-04-05"
  private val status         = MtdStatus.Open

  private val fullResponseModel = RetrieveEOPSObligationsResponse(
    Seq(
      Obligation(MtdBusiness.`self-employment`, businessId, Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open))),
      Obligation(MtdBusiness.`foreign-property`, businessId, Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open))),
      Obligation(MtdBusiness.`uk-property`, businessId, Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open)))
    ))

  private def request(nino: Nino,
                      typeOfBusiness: Option[MtdBusiness],
                      businessId: Option[String],
                      dateRange: Option[(String, String)],
                      status: Option[MtdStatus]) =
    RetrieveEOPSObligationsRequest(
      nino = nino,
      typeOfBusiness = typeOfBusiness,
      businessId = businessId.map(BusinessId),
      dateRange = dateRange.map { case (from, to) => DateRange(LocalDate.parse(from), LocalDate.parse(to)) },
      status = status
    )

  trait Test extends MockRetrieveEOPSObligationsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveEOPSObligationsService(
      connector = mockRetrieveEOPSObligationsConnector
    )

  }

  "service" when {
    "connector call is successful" must {
      "return mapped result with nothing filtered out" when {
        "no typeOfBusiness or businessId are provided" in new Test {
          private val requestData = request(Nino(nino), None, None, Some((fromDate, toDate)), Some(status))

          MockRetrieveEOPSObligationsConnector
            .doConnectorThing(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, fullResponseModel))))

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, fullResponseModel))
        }
      }

      val businesses = Seq(MtdBusiness.`self-employment`, MtdBusiness.`foreign-property`, MtdBusiness.`uk-property`)

      businesses.foreach { business =>
        s"return mapped result with only $business in it" when {
          s"typeOfBusiness [$business] is provided " in new Test {
            private val requestData = request(Nino(nino), Some(business), None, Some((fromDate, toDate)), Some(status))

            private val filteredResponseModel = RetrieveEOPSObligationsResponse(
              Seq(
                Obligation(business, businessId, Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open)))
              ))

            MockRetrieveEOPSObligationsConnector
              .doConnectorThing(requestData)
              .returns(Future.successful(Right(ResponseWrapper(correlationId, fullResponseModel))))

            await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, filteredResponseModel))
          }
        }
      }

      "filter out data with a different businessId" when {
        "businessId is provided and not all of the response objects have that id" in new Test {
          private val requestData = request(Nino(nino), None, Some(businessId), Some((fromDate, toDate)), Some(status))

          private val responseModel = RetrieveEOPSObligationsResponse(
            Seq(
              Obligation(MtdBusiness.`self-employment`, businessId, Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open))),
              Obligation(MtdBusiness.`foreign-property`, businessId, Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open))),
              Obligation(MtdBusiness.`uk-property`, "beans", Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open)))
            ))

          private val filteredResponseModel = RetrieveEOPSObligationsResponse(
            Seq(
              Obligation(MtdBusiness.`self-employment`, businessId, Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open))),
              Obligation(MtdBusiness.`foreign-property`, businessId, Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open)))
            ))

          MockRetrieveEOPSObligationsConnector
            .doConnectorThing(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, filteredResponseModel))
        }
      }

      "return 404" when {
        "typeOfBusiness filter is applied and there are no response objects with that typeOfBusiness" in new Test {
          private val requestData =
            request(Nino(nino), Some(MtdBusiness.`foreign-property`), Some(businessId), Some((fromDate, toDate)), Some(status))

          private val responseModel = RetrieveEOPSObligationsResponse(
            Seq(
              Obligation(MtdBusiness.`uk-property`, businessId, Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open)))
            ))

          MockRetrieveEOPSObligationsConnector
            .doConnectorThing(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
        }
        "businessId filter is applied and there are no response objects with that businessId" in new Test {
          private val requestData =
            request(Nino(nino), Some(MtdBusiness.`foreign-property`), Some(businessId), Some((fromDate, toDate)), Some(status))

          private val responseModel = RetrieveEOPSObligationsResponse(
            Seq(
              Obligation(MtdBusiness.`foreign-property`, "beans", Seq(ObligationDetail(fromDate, toDate, fromDate, Some(toDate), MtdStatus.Open)))
            ))

          MockRetrieveEOPSObligationsConnector
            .doConnectorThing(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
        }

        "the connector call returns an empty Seq due to JSON reads filtering out all obligations" in new Test {
          private val requestData = RetrieveEOPSObligationsRequest(Nino(nino), None, None, None, None)

          private val responseModel = RetrieveEOPSObligationsResponse(Seq())

          MockRetrieveEOPSObligationsConnector
            .doConnectorThing(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
        }
      }
    }
    "connector call is unsuccessful" must {
      "map errors according to spec" when {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            private val requestData =
              request(Nino(nino), Some(typeOfBusiness), Some(businessId), Some((fromDate, toDate)), Some(status))

            MockRetrieveEOPSObligationsConnector
              .doConnectorThing(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = List(
          ("INVALID_IDNUMBER", NinoFormatError),
          ("INVALID_IDTYPE", InternalError),
          ("INVALID_STATUS", InternalError),
          ("INVALID_REGIME", InternalError),
          ("INVALID_DATE_FROM", FromDateFormatError),
          ("INVALID_DATE_TO", ToDateFormatError),
          ("INVALID_DATE_RANGE", RuleDateRangeInvalidError),
          ("INSOLVENT_TRADER", RuleInsolventTraderError),
          ("NOT_FOUND_BPKEY", NotFoundError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )
        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
