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

package v1.models.response.retrieveCrystallisationObligations.des

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.domain.status.DesStatus

class DesRetrieveCrystallisationObligationsResponseSpec extends UnitSpec {
  "reads" should {
    "parse to a model" when {
      "passed obligations with a single item in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSA",
            |                "referenceNumber": "AB123456A",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[DesRetrieveCrystallisationObligationsResponse] shouldBe DesRetrieveCrystallisationObligationsResponse(Seq(
          DesObligation("2018-04-06", "2019-04-05", "1920-01-31", status = DesStatus.F, Some("2020-01-25"))
        ))
      }
      "passed obligations with multiple items in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSA",
            |                "referenceNumber": "AB123456A",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2017-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[DesRetrieveCrystallisationObligationsResponse] shouldBe DesRetrieveCrystallisationObligationsResponse(Seq(
          DesObligation("2018-04-06", "2019-04-05", "1920-01-31", status = DesStatus.F, Some("2020-01-25")),
          DesObligation("2017-04-06", "2019-04-05", "1920-01-31", status = DesStatus.O, Some("2020-01-25"))
        ))
      }
      "passed obligations with a single item in the obligationDetails array and multiple items in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSA",
            |                "referenceNumber": "AB123456A",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSA",
            |                "referenceNumber": "AB123456A",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1921-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[DesRetrieveCrystallisationObligationsResponse] shouldBe DesRetrieveCrystallisationObligationsResponse(Seq(
          DesObligation("2018-04-06", "2019-04-05", "1920-01-31", status = DesStatus.F, Some("2020-01-25")),
          DesObligation("2018-04-06", "2019-04-05", "1921-01-31", status = DesStatus.O, Some("2020-01-25"))
        ))
      }
      "passed obligations with multiple items in the obligationDetails array and multiple items in the obligations arrays" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSA",
            |                "referenceNumber": "AB123456A",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2017-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSA",
            |                "referenceNumber": "AB123456A",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2017-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[DesRetrieveCrystallisationObligationsResponse] shouldBe DesRetrieveCrystallisationObligationsResponse(Seq(
          DesObligation("2018-04-06", "2019-04-05", "1920-01-31", DesStatus.F, Some("2020-01-25")),
          DesObligation("2017-04-06", "2019-04-05", "1920-01-31", DesStatus.O, Some("2020-01-25")),
          DesObligation("2018-04-06", "2019-04-05", "1920-01-31", DesStatus.F, Some("2020-01-25")),
          DesObligation("2017-04-06", "2019-04-05", "1920-01-31", DesStatus.O, Some("2020-01-25"))
        ))
      }
    }
  }

  it should {
    "filter out objects without ITSA incomeSourceType" when {
      "passed obligations with a single item in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "OTHER INCOME SOURCE TYPE",
            |                "referenceNumber": "AB123456A",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[DesRetrieveCrystallisationObligationsResponse] shouldBe DesRetrieveCrystallisationObligationsResponse(Seq())
      }
      "passed obligations with a single item in the obligationDetails array and multiple items in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSA",
            |                "referenceNumber": "AB123456A",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "OTHER INCOME SOURCE TYPE",
            |                "referenceNumber": "AB123456A",
            |                "referenceType": "NINO"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1921-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[DesRetrieveCrystallisationObligationsResponse] shouldBe DesRetrieveCrystallisationObligationsResponse(Seq(
          DesObligation("2018-04-06", "2019-04-05", "1920-01-31", DesStatus.F, Some("2020-01-25"))
        ))
      }
    }
  }
}
