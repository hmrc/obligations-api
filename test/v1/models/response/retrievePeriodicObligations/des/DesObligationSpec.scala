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

package v1.models.response.retrievePeriodicObligations.des

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.domain.business.DesBusiness
import v1.models.domain.status.DesStatus

class DesObligationSpec extends UnitSpec {

  val obligationDetails = DesObligationDetail("2018-04-06", "2019-04-05", "2020-01-31", DesStatus.F, Some("1920-01-31"), "#001")

  "reads" should {
    "read to a model" when {
      "passed JSON with incomeSourceType ITSB" in {
        val json = Json.parse(
          """
            |{
            |"identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "IncomeSourceId"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "1920-01-31",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |
            |
            |""".stripMargin)

        val model = DesObligation(DesBusiness.ITSB, "XAIS12345678910", "IncomeSourceId", Seq(obligationDetails))

        json.as[DesObligation] shouldBe model
      }
      "passed JSON with incomeSourceType ITSP" in {
        val json = Json.parse(
          """
            |{
            |"identification": {
            |                "incomeSourceType": "ITSP",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "IncomeSourceId"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "1920-01-31",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |
            |
            |""".stripMargin)

        val model = DesObligation(DesBusiness.ITSP, "XAIS12345678910", "IncomeSourceId", Seq(obligationDetails))

        json.as[DesObligation] shouldBe model
      }
      "passed JSON with incomeSourceType ITSF" in {
        val json = Json.parse(
          """
            |{
            |"identification": {
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "IncomeSourceId"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "1920-01-31",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |
            |
            |""".stripMargin)

        val model = DesObligation(DesBusiness.ITSF, "XAIS12345678910", "IncomeSourceId", Seq(obligationDetails))

        json.as[DesObligation] shouldBe model

      }
    }
  }

}
