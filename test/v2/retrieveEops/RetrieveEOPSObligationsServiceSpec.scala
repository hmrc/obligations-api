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

package v2.retrieveEops

import api.controllers.EndpointLogContext
import api.models.domain.business.{DesBusiness, MtdBusiness}
import api.models.domain.status.MtdStatus
import api.models.domain.{BusinessId, DateRange, Nino}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.MockRetrieveObligationsConnector
import v2.models.response.domain.ObligationsFixture
import v2.models.response.downstream.{DownstreamObligationDetail, DownstreamObligations, DownstreamObligationsFixture}
import v2.retrieveEops.model.request.RetrieveEOPSObligationsRequest
import v2.retrieveEops.model.response.RetrieveEOPSObligationsResponse

import java.time.LocalDate
import scala.concurrent.Future

class RetrieveEOPSObligationsServiceSpec extends ServiceSpec with DownstreamObligationsFixture with ObligationsFixture {

  private val nino     = "AA123456A"
  private val fromDate = "2018-04-06"
  private val toDate   = "2019-04-05"
  private val status   = MtdStatus.Open

  private def request(nino: Nino,
                      typeOfBusiness: Option[MtdBusiness] = None,
                      businessId: Option[String] = None,
                      dateRange: Option[(String, String)] = None,
                      status: Option[MtdStatus] = None) =
    RetrieveEOPSObligationsRequest(
      nino = nino,
      typeOfBusiness = typeOfBusiness,
      businessId = businessId.map(BusinessId),
      dateRange = dateRange.map { case (from, to) => DateRange(LocalDate.parse(from), LocalDate.parse(to)) },
      status = status
    )

  trait Test extends MockRetrieveObligationsConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveEOPSObligationsService(
      connector = mockRetrieveObligationsConnector
    )

    def eopsDownstreamObligationDetail(inboundCorrespondenceFromDate: String = Defaults.fromDate): DownstreamObligationDetail =
      downstreamObligationDetail(periodKey = "EOPS", inboundCorrespondenceFromDate = inboundCorrespondenceFromDate)

  }

  "service" when {
    "connector call is successful" when {

      "only the date range and status are provided" must {
        "pass these to the connector and return mapped result with nothing filtered out" in new Test {
          val date1 = "2001-01-01"
          val date2 = "2002-02-02"

          private val requestData = request(Nino(nino), dateRange = Some((fromDate, toDate)), status = Some(status))

          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification()),
                obligationDetails = Seq(
                  eopsDownstreamObligationDetail(inboundCorrespondenceFromDate = date1),
                  eopsDownstreamObligationDetail(inboundCorrespondenceFromDate = date2)
                )
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, Some(DateRange.parse(fromDate, toDate)), Some(status)) returns
            Future.successful(Right(ResponseWrapper(correlationId, downstreamResult)))

          private val result = RetrieveEOPSObligationsResponse(
            Seq(
              obligation(obligationDetails = Seq(
                obligationDetail(periodStartDate = date1),
                obligationDetail(periodStartDate = date2)
              ))
            ))

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "some obligations are non-EOPs" must {
        "remove them" in new Test {

          // To label obligation details so we can tell them apart...
          val eopsDate    = "2001-01-01"
          val nonEopsDate = "2002-02-02"

          private val requestData = request(Nino(nino))

          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification()),
                obligationDetails = Seq(
                  eopsDownstreamObligationDetail(inboundCorrespondenceFromDate = eopsDate),
                  downstreamObligationDetail(inboundCorrespondenceFromDate = nonEopsDate)
                )
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, None, None) returns
            Future.successful(Right(ResponseWrapper(correlationId, downstreamResult)))

          private val result = RetrieveEOPSObligationsResponse(
            Seq(
              obligation(obligationDetails = Seq(
                obligationDetail(periodStartDate = eopsDate)
              ))
            ))

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "some obligations are not MTDBIS" must {
        "remove them" in new Test {
          private val requestData = request(Nino(nino))

          private val downstreamResult = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(referenceNumber = "businessId1", referenceType = "MTDBIS")),
                obligationDetails = Seq(eopsDownstreamObligationDetail())
              ),
              downstreamObligation(
                identification = Some(downstreamIdentification(referenceNumber = "businessId2", referenceType = "OTHER")),
                obligationDetails = Seq(eopsDownstreamObligationDetail())
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, None, None) returns
            Future.successful(Right(ResponseWrapper(correlationId, downstreamResult)))

          private val result =
            RetrieveEOPSObligationsResponse(Seq(obligation(businessId = "businessId1", obligationDetails = Seq(obligationDetail()))))

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      Seq(MtdBusiness.`self-employment`, MtdBusiness.`foreign-property`, MtdBusiness.`uk-property`)
        .foreach { businessType =>
          s"typeOfBusiness $businessType is provided" must {
            s"return mapped result with only $businessType in it" in new Test {
              private val requestData = request(Nino(nino), typeOfBusiness = Some(businessType))

              private val downstreamResultAllBusinessTypes = DownstreamObligations(
                Seq(
                  downstreamObligation(
                    identification = Some(downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSB))),
                    obligationDetails = Seq(eopsDownstreamObligationDetail())
                  ),
                  downstreamObligation(
                    identification = Some(downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSP))),
                    obligationDetails = Seq(eopsDownstreamObligationDetail())
                  ),
                  downstreamObligation(
                    identification = Some(downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSF))),
                    obligationDetails = Seq(eopsDownstreamObligationDetail())
                  )
                ))

              MockRetrieveObligationsConnector
                .retrieveObligations(requestData.nino, None, None)
                .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResultAllBusinessTypes))))

              private val result = RetrieveEOPSObligationsResponse(
                Seq(obligation(typeOfBusiness = businessType, obligationDetails = Seq(obligationDetail())))
              )

              await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
            }
          }
        }

      "businessId is provided and not all of the response objects have that id" must {
        "filter out data with a different businessId" in new Test {
          private val businessId  = "someBusinessId"
          private val requestData = request(Nino(nino), businessId = Some(businessId))

          private val downstreamResultMultipleBusinessIds = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(referenceNumber = businessId)),
                obligationDetails = Seq(eopsDownstreamObligationDetail())
              ),
              downstreamObligation(
                identification = Some(downstreamIdentification(referenceNumber = "otherBusinessId")),
                obligationDetails = Seq(eopsDownstreamObligationDetail())
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, None, None)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResultMultipleBusinessIds))))

          private val result = RetrieveEOPSObligationsResponse(
            Seq(obligation(businessId = businessId, obligationDetails = Seq(obligationDetail())))
          )

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "obligation details are empty for an obligation" must {
        "remove that obligation" in new Test {
          private val requestData = request(Nino(nino))

          private val downstreamResultMultipleBusinessIds = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(referenceNumber = "businessId")),
                obligationDetails = Seq(eopsDownstreamObligationDetail())
              ),
              downstreamObligation(
                identification = Some(downstreamIdentification(referenceNumber = "businessId_NoDetails")),
                obligationDetails = Nil
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, None, None)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResultMultipleBusinessIds))))

          private val result = RetrieveEOPSObligationsResponse(
            Seq(obligation(businessId = "businessId", obligationDetails = Seq(obligationDetail())))
          )

          await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, result))
        }
      }

      "filtering removes all obligations" must {
        "return NoObligationsFoundError" in new Test {
          private val requestData = request(Nino(nino), businessId = Some("businessId"))

          private val downstreamResultMultipleBusinessIds = DownstreamObligations(
            Seq(
              downstreamObligation(
                identification = Some(downstreamIdentification(referenceNumber = "otherBusinessId")),
                obligationDetails = Seq(eopsDownstreamObligationDetail())
              )
            ))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, None, None)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResultMultipleBusinessIds))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
        }
      }

      "the connector call returns an empty obligations" must {
        "return NoObligationsFoundError" in new Test {
          private val requestData = request(Nino(nino))

          MockRetrieveObligationsConnector
            .retrieveObligations(requestData.nino, None, None)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, DownstreamObligations(Nil)))))

          await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoObligationsFoundError))
        }
      }
    }

    "connector call is unsuccessful" must {
      "map errors according to spec" when {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {
            private val requestData = request(Nino(nino))

            MockRetrieveObligationsConnector
              .retrieveObligations(requestData.nino, None, None)
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
