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

package v1.models.response.retrieveCrystallisationObligations

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.domain.status.MtdStatus

class RetrieveCrystallisationObligationsResponseSpec extends UnitSpec {
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
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveCrystallisationObligationsResponse] shouldBe RetrieveCrystallisationObligationsResponse(Seq(
          Obligation(periodStartDate = "2018-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Fulfilled, receivedDate = Some("2020-01-25"))
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
            |                    "periodKey": "ITSA"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2017-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveCrystallisationObligationsResponse] shouldBe RetrieveCrystallisationObligationsResponse(Seq(
          Obligation(periodStartDate = "2018-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Fulfilled, receivedDate = Some("2020-01-25")),
          Obligation(periodStartDate = "2017-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Open, receivedDate = Some("2020-01-25"))
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
            |                    "periodKey": "ITSA"
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
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveCrystallisationObligationsResponse] shouldBe RetrieveCrystallisationObligationsResponse(Seq(
          Obligation(periodStartDate = "2018-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Fulfilled, receivedDate = Some("2020-01-25")),
          Obligation(periodStartDate = "2018-04-06", periodEndDate = "2019-04-05", dueDate = "1921-01-31",status = MtdStatus.Open, receivedDate = Some("2020-01-25"))
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
            |                    "periodKey": "ITSA"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2017-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "ITSA"
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
            |                    "periodKey": "ITSA"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2017-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveCrystallisationObligationsResponse] shouldBe RetrieveCrystallisationObligationsResponse(Seq(
          Obligation(periodStartDate = "2018-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Fulfilled, receivedDate = Some("2020-01-25")),
          Obligation(periodStartDate = "2017-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Open, receivedDate = Some("2020-01-25")),
          Obligation(periodStartDate = "2018-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Fulfilled, receivedDate = Some("2020-01-25")),
          Obligation(periodStartDate = "2017-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Open, receivedDate = Some("2020-01-25"))
        ))
      }
    }
    "filter out objects without ITSA periodKey" when {
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
            |                    "periodKey": "OTHER PERIOD KEY"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveCrystallisationObligationsResponse] shouldBe RetrieveCrystallisationObligationsResponse(Seq())
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
            |                    "periodKey": "OTHER PERIOD KEY"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2017-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveCrystallisationObligationsResponse] shouldBe RetrieveCrystallisationObligationsResponse(Seq(
          Obligation(periodStartDate = "2017-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Open, receivedDate = Some("2020-01-25"))
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
            |                    "periodKey": "ITSA"
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
            |                    "periodKey": "OTHER PERIOD KEY"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveCrystallisationObligationsResponse] shouldBe RetrieveCrystallisationObligationsResponse(Seq(
          Obligation(periodStartDate = "2018-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Fulfilled, receivedDate = Some("2020-01-25"))
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
            |                    "periodKey": "ITSA"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2017-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "ITSA"
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
            |                    "periodKey": "OTHER PERIOD KEY"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2017-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveCrystallisationObligationsResponse] shouldBe RetrieveCrystallisationObligationsResponse(Seq(
          Obligation(periodStartDate = "2018-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Fulfilled, receivedDate = Some("2020-01-25")),
          Obligation(periodStartDate = "2017-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Open, receivedDate = Some("2020-01-25")),
          Obligation(periodStartDate = "2017-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Open, receivedDate = Some("2020-01-25"))
        ))
      }
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(RetrieveCrystallisationObligationsResponse(Seq(
        Obligation(periodStartDate = "2018-04-06", periodEndDate = "2019-04-05", dueDate = "1920-01-31",status = MtdStatus.Fulfilled, receivedDate = Some("2020-01-25"))
      ))) shouldBe Json.parse(
        """
          |{
          |  "obligationDetails": [
          |    {
          |      "periodStartDate": "2018-04-06",
          |      "periodEndDate": "2019-04-05",
          |      "dueDate": "1920-01-31",
          |      "status": "Fulfilled",
          |      "receivedDate": "2020-01-25"
          |    }
          |  ]
          |}
          |""".stripMargin)
    }
  }
}
