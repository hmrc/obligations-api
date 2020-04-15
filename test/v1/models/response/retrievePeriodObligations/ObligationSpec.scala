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

package v1.models.response.retrievePeriodObligations

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.MtdStatus

class ObligationSpec extends UnitSpec {
  "writes" should {
    "write to JSON" when {
      val obligationDetails = ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Fulfilled)

      "passed a model with typeOfBusiness self-employment" in {
        val json = Json.parse(
          """
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

        val model = Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(obligationDetails))

        Json.toJson(model) shouldBe json
      }
      "passed a model with typeOfBusiness uk-property" in {
        val json = Json.parse(
          """
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

        val model = Obligation(MtdBusiness.`uk-property`, "XAIS12345678910", Seq(obligationDetails))

        Json.toJson(model) shouldBe json
      }
      "passed a model with typeOfBusiness foreign-property" in {
        val json = Json.parse(
          """
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

        val model = Obligation(MtdBusiness.`foreign-property`, "XAIS12345678910", Seq(obligationDetails))

        Json.toJson(model) shouldBe json
      }
    }
  }

}
