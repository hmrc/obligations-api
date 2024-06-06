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

package v2.models.response.domain

import api.models.domain.business.{DesBusiness, MtdBusiness}
import api.models.domain.status.MtdStatus
import play.api.libs.json.Json
import support.UnitSpec
import v2.models.response.downstream.DownstreamObligationsFixture

class BusinessObligationSpec extends UnitSpec with DownstreamObligationsFixture with ObligationsFixture {

  "fromDownstream" should {
    "map fields correctly" in {
      BusinessObligation.fromDownstream(
        downstreamObligation(
          Some(
            downstreamIdentification(
              incomeSourceType = Some(Defaults.incomeSourceType),
              referenceNumber = Defaults.referenceNumber,
              referenceType = Defaults.referenceType)),
          Seq(downstreamObligationDetail())
        )) shouldBe
        Some(obligation(typeOfBusiness = Defaults.typeOfBusiness, businessId = Defaults.businessId, Seq(obligationDetail())))
    }

    behave like mapBusinessType(DesBusiness.ITSB, MtdBusiness.`self-employment`)
    behave like mapBusinessType(DesBusiness.ITSF, MtdBusiness.`foreign-property`)
    behave like mapBusinessType(DesBusiness.ITSP, MtdBusiness.`uk-property`)

    def mapBusinessType(incomeSourceType: DesBusiness, expectedTypeOfBusiness: MtdBusiness): Unit =
      s"map income source type $incomeSourceType to $expectedTypeOfBusiness" in {
        BusinessObligation.fromDownstream(
          downstreamObligation(
            Some(downstreamIdentification(incomeSourceType = Some(incomeSourceType))),
            Seq(downstreamObligationDetail())
          )) shouldBe
          Some(obligation(typeOfBusiness = expectedTypeOfBusiness, obligationDetails = Seq(obligationDetail())))
      }

    "return None if there is not identification in the downstream obligation" in {
      BusinessObligation.fromDownstream(
        downstreamObligation(
          None,
          Seq(downstreamObligationDetail())
        )) shouldBe None
    }

    "return None if the identification in the downstream obligation has no incomeSourceType" in {
      BusinessObligation.fromDownstream(
        downstreamObligation(
          Some(downstreamIdentification(incomeSourceType = None)),
          Seq(downstreamObligationDetail())
        )) shouldBe None
    }

    "return None if the incomeSourceType has no corresponding MTD business type" in {
      BusinessObligation.fromDownstream(
        downstreamObligation(
          Some(downstreamIdentification(incomeSourceType = Some(DesBusiness.ITSA))),
          Seq(downstreamObligationDetail())
        )) shouldBe None
    }

    "return None if the referenceType is not MTDBIS" in {
      BusinessObligation.fromDownstream(
        downstreamObligation(
          Some(downstreamIdentification(referenceType = "OTHER")),
          Seq(downstreamObligationDetail())
        )) shouldBe None
    }
  }

  "writes" should {
    "write to JSON" when {
      val obligationDetails = ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Fulfilled)

      "passed a model with typeOfBusiness self-employment" in {
        val json = Json.parse("""
            |{
            |       "typeOfBusiness": "self-employment",
            |       "businessId": "XAIS12345678910",
            |       "obligationDetails": [
            |         {
            |           "periodStartDate": "2018-04-06",
            |           "periodEndDate": "2019-04-05",
            |           "dueDate": "1920-01-31",
            |           "receivedDate": "2020-01-25",
            |           "status": "Fulfilled"
            |         }
            |    ]
            |}
            |""".stripMargin)

        val model = BusinessObligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(obligationDetails))

        Json.toJson(model) shouldBe json
      }
      "passed a model with typeOfBusiness uk-property" in {
        val json = Json.parse("""
            |{
            |       "typeOfBusiness": "uk-property",
            |       "businessId": "XAIS12345678910",
            |       "obligationDetails": [
            |         {
            |           "periodStartDate": "2018-04-06",
            |           "periodEndDate": "2019-04-05",
            |           "dueDate": "1920-01-31",
            |           "receivedDate": "2020-01-25",
            |           "status": "Fulfilled"
            |         }
            |    ]
            |}
            |""".stripMargin)

        val model = BusinessObligation(MtdBusiness.`uk-property`, "XAIS12345678910", Seq(obligationDetails))

        Json.toJson(model) shouldBe json
      }
      "passed a model with typeOfBusiness foreign-property" in {
        val json = Json.parse("""
            |{
            |       "typeOfBusiness": "foreign-property",
            |       "businessId": "XAIS12345678910",
            |       "obligationDetails": [
            |         {
            |           "periodStartDate": "2018-04-06",
            |           "periodEndDate": "2019-04-05",
            |           "dueDate": "1920-01-31",
            |           "receivedDate": "2020-01-25",
            |           "status": "Fulfilled"
            |         }
            |    ]
            |}
            |""".stripMargin)

        val model = BusinessObligation(MtdBusiness.`foreign-property`, "XAIS12345678910", Seq(obligationDetails))

        Json.toJson(model) shouldBe json
      }
    }
  }

}
