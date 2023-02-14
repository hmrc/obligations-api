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

package api.support

import api.controllers.EndpointLogContext
import api.models.domain.business.MtdBusiness
import api.models.domain.status.{ DesStatus, MtdStatus }
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.http.Status.BAD_REQUEST
import support.UnitSpec
import utils.Logging
import v1.models.response.common.{ Obligation, ObligationDetail }
import v1.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse
import v1.models.response.retrieveCrystallisationObligations.des.{ DesObligation, DesRetrieveCrystallisationObligationsResponse }
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse
import v1.models.response.retrievePeriodicObligations.RetrievePeriodObligationsResponse

class DownstreamResponseMappingSupportSpec extends UnitSpec {

  implicit val logContext: EndpointLogContext                = EndpointLogContext("ctrl", "ep")
  val mapping: DownstreamResponseMappingSupport with Logging = new DownstreamResponseMappingSupport with Logging {}

  val correlationId = "someCorrelationId"

  object Error1 extends MtdError("msg", "code1", BAD_REQUEST)

  object Error2 extends MtdError("msg", "code2", BAD_REQUEST)

  object ErrorBvrMain extends MtdError("msg", "bvrMain", BAD_REQUEST)

  object ErrorBvr extends MtdError("msg", "bvr", BAD_REQUEST)

  val errorCodeMap: PartialFunction[String, MtdError] = {
    case "ERR1" => Error1
    case "ERR2" => Error2
    case "DS"   => InternalError
  }

  "mapping Downstream errors" when {
    "single error" when {
      "the error code is in the map provided" must {
        "use the mapping and wrap" in {
          mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("ERR1")))) shouldBe
            ErrorWrapper(correlationId, Error1)
        }
      }

      "the error code is not in the map provided" must {
        "default to DownstreamError and wrap" in {
          mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("UNKNOWN")))) shouldBe
            ErrorWrapper(correlationId, InternalError)
        }
      }
    }

    "multiple errors" when {
      "the error codes is in the map provided" must {
        "use the mapping and wrap with main error type of BadRequest" in {
          mapping.mapDownstreamErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("ERR2"))))) shouldBe
            ErrorWrapper(correlationId, BadRequestError, Some(Seq(Error1, Error2)))
        }
      }

      "the error code is not in the map provided" must {
        "default main error to DownstreamError ignore other errors" in {
          mapping.mapDownstreamErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("UNKNOWN"))))) shouldBe
            ErrorWrapper(correlationId, InternalError)
        }
      }

      "one of the mapped errors is DownstreamError" must {
        "wrap the errors with main error type of DownstreamError" in {
          mapping.mapDownstreamErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("DS"))))) shouldBe
            ErrorWrapper(correlationId, InternalError)
        }
      }
    }

    "the error code is an OutboundError" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain))) shouldBe
          ErrorWrapper(correlationId, ErrorBvrMain)
      }
    }

    "the error code is an OutboundError with multiple errors" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain, Some(Seq(ErrorBvr))))) shouldBe
          ErrorWrapper(correlationId, ErrorBvrMain, Some(Seq(ErrorBvr)))
      }
    }
  }

  "filterEOPSValues" should {
    "return an unfiltered model" when {
      "no filters are applied" in {
        val model = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, None) shouldBe Right(ResponseWrapper(correlationId, model))
      }
      "no obligations are filtered out due to applied filters" in {
        val model = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`self-employment`), Some("id123")) shouldBe Right(
          ResponseWrapper(correlationId, model))
      }
    }
    "return a filtered model" when {
      "typeOfBusiness filter is applied and obligations with a different typeOfBusiness are found" in {
        val model = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        val filteredModel = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`self-employment`), None) shouldBe Right(
          ResponseWrapper(correlationId, filteredModel))
      }
      "businessId filter is applied and obligations with a different businessId are found" in {
        val model = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        val filteredModel = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, Some("id123")) shouldBe Right(
          ResponseWrapper(correlationId, filteredModel))
      }
      "a model with at least one obligation with no obligationDetails is supplied" in {
        val model = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`uk-property`, "id124", Seq()),
            Obligation(MtdBusiness.`self-employment`, "id125", Seq())
          ))
        val filteredModel = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, None) shouldBe Right(ResponseWrapper(correlationId, filteredModel))
      }
    }
    "return an error" when {
      "a model with no obligations is supplied" in {
        val model = RetrieveEOPSObligationsResponse(Seq())
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, None) shouldBe Left(
          ErrorWrapper(correlationId, NoObligationsFoundError))
      }
      "a model with no obligationDetails is supplied" in {
        val model = RetrieveEOPSObligationsResponse(Seq(Obligation(MtdBusiness.`uk-property`, "id123", Seq())))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, None) shouldBe Left(
          ErrorWrapper(correlationId, NoObligationsFoundError))
      }
      "the typeOfBusiness filter filters out everything" in {
        val model = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`uk-property`), None) shouldBe Left(
          ErrorWrapper(correlationId, NoObligationsFoundError))
      }
      "the businessId filter filters out everything" in {
        val model = RetrieveEOPSObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, Some("beans")) shouldBe Left(
          ErrorWrapper(correlationId, NoObligationsFoundError))
      }
    }
  }

  "filterCrystallisationValues" when {
    "passed a valid DES model" should {
      "return an MTD model" in {
        val desModel = DesRetrieveCrystallisationObligationsResponse(
          Seq(
            DesObligation("", "", "", DesStatus.F, None)
          ))
        val mtdModel = RetrieveCrystallisationObligationsResponse(
          "",
          "",
          "",
          MtdStatus.Fulfilled,
          None
        )
        mapping.filterCrystallisationValues(ResponseWrapper(correlationId, desModel)) shouldBe Right(ResponseWrapper(correlationId, mtdModel))
      }
    }
    "passed a DES model with nothing in the array" should {
      "return a NO_OBLIGATIONS_FOUND error" in {
        val desModel = DesRetrieveCrystallisationObligationsResponse(Seq())
        mapping.filterCrystallisationValues(ResponseWrapper(correlationId, desModel)) shouldBe Left(
          ErrorWrapper(correlationId, NoObligationsFoundError))
      }
    }
    "passed a DES model with more than one object in the array" should {
      "return an INTERNAL_SERVER_ERROR error" in {
        val desModel = DesRetrieveCrystallisationObligationsResponse(
          Seq(
            DesObligation("", "", "", DesStatus.F, None),
            DesObligation("", "", "", DesStatus.O, None)
          ))
        mapping.filterCrystallisationValues(ResponseWrapper(correlationId, desModel)) shouldBe Left(ErrorWrapper(correlationId, InternalError))
      }
    }
  }

  "filterPeriodicValues" should {
    "return an unfiltered model" when {
      "no filters are applied" in {
        val model = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, None) shouldBe Right(ResponseWrapper(correlationId, model))
      }
      "no obligations are filtered out due to applied filters" in {
        val model = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`self-employment`), Some("id123")) shouldBe Right(
          ResponseWrapper(correlationId, model))
      }
    }
    "return a filtered model" when {
      "typeOfBusiness filter is applied and obligations with a different typeOfBusiness are found" in {
        val model = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        val filteredModel = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`self-employment`), None) shouldBe Right(
          ResponseWrapper(correlationId, filteredModel))
      }
      "businessId filter is applied and obligations with a different businessId are found" in {
        val model = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        val filteredModel = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, Some("id123")) shouldBe Right(
          ResponseWrapper(correlationId, filteredModel))
      }
      "a model with at least one obligation with no obligationDetails is supplied" in {
        val model = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
            Obligation(MtdBusiness.`uk-property`, "id124", Seq()),
            Obligation(MtdBusiness.`self-employment`, "id125", Seq())
          ))
        val filteredModel = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, None) shouldBe Right(ResponseWrapper(correlationId, filteredModel))
      }
    }
    "return an error" when {
      "a model with no obligations is supplied" in {
        val model = RetrievePeriodObligationsResponse(Seq())
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, None) shouldBe Left(
          ErrorWrapper(correlationId, NoObligationsFoundError))
      }
      "a model with no obligationDetails is supplied" in {
        val model = RetrievePeriodObligationsResponse(Seq(Obligation(MtdBusiness.`uk-property`, "id123", Seq())))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, None) shouldBe Left(
          ErrorWrapper(correlationId, NoObligationsFoundError))
      }
      "the typeOfBusiness filter filters out everything" in {
        val model = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`uk-property`), None) shouldBe Left(
          ErrorWrapper(correlationId, NoObligationsFoundError))
      }
      "the businessId filter filters out everything" in {
        val model = RetrievePeriodObligationsResponse(
          Seq(
            Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
          ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, Some("beans")) shouldBe Left(
          ErrorWrapper(correlationId, NoObligationsFoundError))
      }
    }
  }
}
