/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.support

import support.UnitSpec
import utils.Logging
import v1.controllers.EndpointLogContext
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.{DesStatus, MtdStatus}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.common.{Obligation, ObligationDetail}
import v1.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse
import v1.models.response.retrieveCrystallisationObligations.des.{DesObligation, DesRetrieveCrystallisationObligationsResponse}
import v1.models.response.retrieveEOPSObligations.RetrieveEOPSObligationsResponse
import v1.models.response.retrievePeriodicObligations.RetrievePeriodObligationsResponse

class DesResponseMappingSupportSpec extends UnitSpec {

  implicit val logContext: EndpointLogContext = EndpointLogContext("ctrl", "ep")
  val mapping: DesResponseMappingSupport with Logging = new DesResponseMappingSupport with Logging {}

  val correlationId = "someCorrelationId"

  object Error1 extends MtdError("msg", "code1")

  object Error2 extends MtdError("msg", "code2")

  object ErrorBvrMain extends MtdError("msg", "bvrMain")

  object ErrorBvr extends MtdError("msg", "bvr")

  val errorCodeMap : PartialFunction[String, MtdError] = {
    case "ERR1" => Error1
    case "ERR2" => Error2
    case "DS" => DownstreamError
  }

  "mapping Des errors" when {
    "single error" when {
      "the error code is in the map provided" must {
        "use the mapping and wrap" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("ERR1")))) shouldBe
            ErrorWrapper(Some(correlationId), Error1)
        }
      }

      "the error code is not in the map provided" must {
        "default to DownstreamError and wrap" in {
          mapping.mapDesErrors (errorCodeMap)(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("UNKNOWN")))) shouldBe
            ErrorWrapper(Some(correlationId), DownstreamError)
        }
      }
    }

    "multiple errors" when {
      "the error codes is in the map provided" must {
        "use the mapping and wrap with main error type of BadRequest" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("ERR1"), DesErrorCode("ERR2"))))) shouldBe
            ErrorWrapper(Some(correlationId), BadRequestError, Some(Seq(Error1, Error2)))
        }
      }

      "the error code is not in the map provided" must {
        "default main error to DownstreamError ignore other errors" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("ERR1"), DesErrorCode("UNKNOWN"))))) shouldBe
            ErrorWrapper(Some(correlationId), DownstreamError)
        }
      }

      "one of the mapped errors is DownstreamError" must {
        "wrap the errors with main error type of DownstreamError" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("ERR1"), DesErrorCode("DS"))))) shouldBe
            ErrorWrapper(Some(correlationId), DownstreamError)
        }
      }
    }

    "the error code is an OutboundError" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain))) shouldBe
          ErrorWrapper(Some(correlationId), ErrorBvrMain)
      }
    }

    "the error code is an OutboundError with multiple errors" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain, Some(Seq(ErrorBvr))))) shouldBe
          ErrorWrapper(Some(correlationId), ErrorBvrMain, Some(Seq(ErrorBvr)))
      }
    }
  }

  "filterEOPSValues" should {
    "return an unfiltered model" when {
      "no filters are applied" in {
        val model = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, None) shouldBe Right(ResponseWrapper(correlationId, model))
      }
      "no obligations are filtered out due to applied filters" in {
        val model = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`self-employment`), Some("id123")) shouldBe Right(ResponseWrapper(correlationId, model))
      }
    }
    "return a filtered model" when {
      "typeOfBusiness filter is applied and obligations with a different typeOfBusiness are found" in {
        val model = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        val filteredModel = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`self-employment`), None) shouldBe Right(ResponseWrapper(correlationId, filteredModel))
      }
      "businessId filter is applied and obligations with a different businessId are found" in {
        val model = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        val filteredModel = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, Some("id123")) shouldBe Right(ResponseWrapper(correlationId, filteredModel))
      }
      "a model with at least one obligation with no obligationDetails is supplied" in {
        val model = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`uk-property`, "id124", Seq()),
          Obligation(MtdBusiness.`self-employment`, "id125", Seq())
        ))
        val filteredModel = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, None) shouldBe Right(ResponseWrapper(correlationId, filteredModel))
      }
    }
    "return an error" when {
      "a model with no obligations is supplied" in {
        val model = RetrieveEOPSObligationsResponse(Seq())
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, None) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
      "a model with no obligationDetails is supplied" in {
        val model = RetrieveEOPSObligationsResponse(Seq(Obligation(MtdBusiness.`uk-property`, "id123", Seq())))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, None) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
      "the typeOfBusiness filter filters out everything" in {
        val model = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`uk-property`), None) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
      "the businessId filter filters out everything" in {
        val model = RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterEOPSValues(ResponseWrapper(correlationId, model), None, Some("beans")) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
    }
  }
  "filterCrystallisationValues" when {
    "passed a valid DES model" should {
      "return an MTD model" in {
        val desModel = DesRetrieveCrystallisationObligationsResponse(Seq(
          DesObligation("", "", "", DesStatus.F, None)
        ))
        val mtdModel = RetrieveCrystallisationObligationsResponse(
          "", "", "", MtdStatus.Fulfilled, None
        )
        mapping.filterCrystallisationValues(ResponseWrapper(correlationId, desModel)) shouldBe Right(ResponseWrapper(correlationId, mtdModel))
      }
    }
    "passed a DES model with nothing in the array" should {
      "return a NO_OBLIGATIONS_FOUND error" in {
        val desModel = DesRetrieveCrystallisationObligationsResponse(Seq())
        mapping.filterCrystallisationValues(ResponseWrapper(correlationId, desModel)) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
    }
    "passed a DES model with more than one object in the array" should {
      "return an INTERNAL_SERVER_ERROR error" in {
        val desModel = DesRetrieveCrystallisationObligationsResponse(Seq(
          DesObligation("", "", "", DesStatus.F, None),
          DesObligation("", "", "", DesStatus.O, None)
        ))
        mapping.filterCrystallisationValues(ResponseWrapper(correlationId, desModel)) shouldBe Left(ErrorWrapper(Some(correlationId), DownstreamError))
      }
    }
  }

  "filterPeriodicValues" should {
    "return an unfiltered model" when {
      "no filters are applied" in {
        val model = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, None) shouldBe Right(ResponseWrapper(correlationId, model))
      }
      "no obligations are filtered out due to applied filters" in {
        val model = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`self-employment`), Some("id123")) shouldBe Right(ResponseWrapper(correlationId, model))
      }
    }
    "return a filtered model" when {
      "typeOfBusiness filter is applied and obligations with a different typeOfBusiness are found" in {
        val model = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        val filteredModel = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`self-employment`), None) shouldBe Right(ResponseWrapper(correlationId, filteredModel))
      }
      "businessId filter is applied and obligations with a different businessId are found" in {
        val model = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`uk-property`, "id124", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        val filteredModel = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, Some("id123")) shouldBe Right(ResponseWrapper(correlationId, filteredModel))
      }
      "a model with at least one obligation with no obligationDetails is supplied" in {
        val model = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled))),
          Obligation(MtdBusiness.`uk-property`, "id124", Seq()),
          Obligation(MtdBusiness.`self-employment`, "id125", Seq())
        ))
        val filteredModel = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, None) shouldBe Right(ResponseWrapper(correlationId, filteredModel))
      }
    }
    "return an error" when {
      "a model with no obligations is supplied" in {
        val model = RetrievePeriodObligationsResponse(Seq())
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, None) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
      "a model with no obligationDetails is supplied" in {
        val model = RetrievePeriodObligationsResponse(Seq(Obligation(MtdBusiness.`uk-property`, "id123", Seq())))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, None) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
      "the typeOfBusiness filter filters out everything" in {
        val model = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), Some(MtdBusiness.`uk-property`), None) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
      "the businessId filter filters out everything" in {
        val model = RetrievePeriodObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "id123", Seq(ObligationDetail("", "", "", None, MtdStatus.Fulfilled)))
        ))
        mapping.filterPeriodicValues(ResponseWrapper(correlationId, model), None, Some("beans")) shouldBe Left(ErrorWrapper(Some(correlationId), NoObligationsFoundError))
      }
    }
  }
}
