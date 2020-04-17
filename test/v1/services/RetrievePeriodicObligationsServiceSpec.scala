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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.services.MockRetrievePeriodicObligationsConnector
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.{DesStatus, MtdStatus}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrievePeriodObligations.RetrievePeriodicObligationsRequest
import v1.models.response.retrievePeriodObligations.{Obligation, ObligationDetail, RetrievePeriodObligationsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePeriodicObligationsServiceSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validtypeOfBusiness = MtdBusiness.`foreign-property`
  private val validincomeSourceId = "XAIS123456789012"
  private val validfromDate = "2018-04-06"
  private val validtoDate = "2019-04-05"
  private val validStatus = DesStatus.O
  private val correlationId = "X-123"

  private val requestData = RetrievePeriodicObligationsRequest(
    Nino(validNino),
    Some(validtypeOfBusiness),
    Some(validincomeSourceId),
    Some(validfromDate),
    Some(validtoDate),
    Some(validStatus))

  trait Test extends MockRetrievePeriodicObligationsConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrievePeriodicObligationsService(
      connector = mockRetrievePeriodicObligationsConnector
    )
  }

  "service" when {
    "service call is successful" must {
      "return mapped result" in new Test {
        val responseModel = RetrievePeriodObligationsResponse(Seq(
          Obligation(validtypeOfBusiness,
            validincomeSourceId,
            Seq(ObligationDetail(validfromDate,
              validtoDate,
              validfromDate,
              Some(validtoDate),
              MtdStatus.Open)))))

        MockRetrievePeriodicObligationsConnector.doConnectorThing(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseModel))
      }
    }
    "unsuccessful" must {
      "map errors according to spec" when {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrievePeriodicObligationsConnector.doConnectorThing(requestData)
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
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )
        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
