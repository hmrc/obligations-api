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

package v1.models.response.retrieveEOPSObligations

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.domain.business.MtdBusiness
import v1.models.domain.status.MtdStatus
import v1.models.response.common.{Obligation, ObligationDetail}
import v1.models.utils.JsonErrorValidators

class RetrieveEOPSObligationsResponseSpec extends UnitSpec with JsonErrorValidators {

  "reads" should {
    "parse to a model" when {
      "passed obligations with a single item in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-01-01",
            |                    "inboundCorrespondenceToDate": "2018-12-31",
            |                    "inboundCorrespondenceDateReceived": "2019-05-13",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-01-01", "2018-12-31", "2020-01-31", Some("2019-05-13"), MtdStatus.Fulfilled)
          ))
        ))
      }
      "passed obligations with multiple items in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-01-01",
            |                    "inboundCorrespondenceToDate": "2018-12-31",
            |                    "inboundCorrespondenceDateReceived": "2019-05-13",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-03-31",
            |                    "inboundCorrespondenceDateReceived": "2019-04-25",
            |                    "inboundCorrespondenceDueDate": "2019-04-30",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-04-01",
            |                    "inboundCorrespondenceToDate": "2019-06-30",
            |                    "inboundCorrespondenceDateReceived": "2019-07-01",
            |                    "inboundCorrespondenceDueDate": "2019-07-31",
            |                    "periodKey": "#002"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2019-07-01",
            |                    "inboundCorrespondenceToDate": "2019-09-30",
            |                    "inboundCorrespondenceDateReceived": "2019-10-08",
            |                    "inboundCorrespondenceDueDate": "2019-10-31",
            |                    "periodKey": "#003"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-10-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2020-01-31",
            |                    "periodKey": "#004"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2019-01-01",
            |                    "inboundCorrespondenceToDate": "2019-12-31",
            |                    "inboundCorrespondenceDueDate": "2021-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-01-01", "2018-12-31", "2020-01-31", Some("2019-05-13"), MtdStatus.Fulfilled),
            ObligationDetail("2019-01-01", "2019-12-31", "2021-01-31", None,  MtdStatus.Open)
          ))
        ))
      }
      "passed obligations with multiple items in obligations and single items in the obligationDetails array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678911",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Fulfilled)
          )),
          Obligation(MtdBusiness.`foreign-property`, "XAIS12345678911", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Open)
          ))
        ))
      }
      "passed obligations with multiple items in obligations and multiple items in the obligationDetails array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678911",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Fulfilled),
            ObligationDetail("2018-04-06", "2019-04-05", "1930-01-31", Some("2020-01-25"), MtdStatus.Open)
          )),
          Obligation(MtdBusiness.`foreign-property`, "XAIS12345678911", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Open),
            ObligationDetail("2018-04-06", "2019-04-05", "1930-01-31", Some("2020-01-25"), MtdStatus.Fulfilled)
          ))
        ))
      }
      "passed obligations with multiple items in obligations where some of them are ITSA" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSA",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "IncomeSourceId"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678911",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`foreign-property`, "XAIS12345678911", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Open)
          ))
        ))
      }
    }
  }

  "filter" should {
    "remove objects with ITSA periodKey" when {
      "passed obligations with a single item in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq())))
      }
      "passed obligations with a multiple items in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Fulfilled)))
        ))
      }
      "passed obligations with a single item in the obligationDetails array and a multiple items in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678911",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq()),
          Obligation(MtdBusiness.`foreign-property`, "XAIS12345678911", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Open)
          ))
        ))
      }
      "passed obligations with a multiple items in the obligationDetails array and a multiple items in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678911",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1930-01-31", Some("2020-01-25"), MtdStatus.Open)
          )),
          Obligation(MtdBusiness.`foreign-property`, "XAIS12345678911", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Open)))
        ))
      }
    }
    "remove objects without the EOPS periodKey" when {
      "passed obligations with a single item in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq())))
      }
      "passed obligations with a multiple items in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Fulfilled)))
        ))
      }
      "passed obligations with a single item in the obligationDetails array and a multiple items in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678911",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq()),
          Obligation(MtdBusiness.`foreign-property`, "XAIS12345678911", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Open)
          ))
        ))
      }
      "passed obligations with a multiple items in the obligationDetails array and a multiple items in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678911",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "EOPS"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1930-01-31", Some("2020-01-25"), MtdStatus.Open)
          )),
          Obligation(MtdBusiness.`foreign-property`, "XAIS12345678911", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1920-01-31", Some("2020-01-25"), MtdStatus.Open)))
        ))
      }
    }
    "remove objects with numbered or ITSA periodKey within the same obligationDetails array" when {
      "passed obligations with only numbered & ITSA periodKeys in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq())
        ))
      }
      "passed obligations with a multiple items in the obligationDetails array and a single item in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)
        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1930-01-31", Some("2020-01-25"), MtdStatus.Open)
          ))
        ))
      }
      "passed obligations with only numbered & ITSA periodKeys in the obligationDetails array and a multiple items in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "ITSA"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678911",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "ITSA"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "#001"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq()),
          Obligation(MtdBusiness.`foreign-property`, "XAIS12345678911", Seq())
        ))
      }
      "passed obligations with a multiple items in the obligationDetails array and a multiple items in the obligations array" in {
        val desJson = Json.parse(
          """
            |{
            |    "obligations": [
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSB",
            |                "referenceNumber": "XAIS12345678910",
            |                "referenceType": "MTDBIS"
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
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "ITSA"
            |                },
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        },
            |        {
            |            "identification": {
            |                "incomeSourceType": "ITSF",
            |                "referenceNumber": "XAIS12345678911",
            |                "referenceType": "MTDBIS"
            |            },
            |            "obligationDetails": [
            |                {
            |                    "status": "O",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1920-01-31",
            |                    "periodKey": "ITSA"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "#001"
            |                },
            |                {
            |                    "status": "F",
            |                    "inboundCorrespondenceFromDate": "2018-04-06",
            |                    "inboundCorrespondenceToDate": "2019-04-05",
            |                    "inboundCorrespondenceDateReceived": "2020-01-25",
            |                    "inboundCorrespondenceDueDate": "1930-01-31",
            |                    "periodKey": "EOPS"
            |                }
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        desJson.as[RetrieveEOPSObligationsResponse] shouldBe RetrieveEOPSObligationsResponse(Seq(
          Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1930-01-31", Some("2020-01-25"), MtdStatus.Open)
          )),
          Obligation(MtdBusiness.`foreign-property`, "XAIS12345678911", Seq(
            ObligationDetail("2018-04-06", "2019-04-05", "1930-01-31", Some("2020-01-25"), MtdStatus.Fulfilled)
          ))
        ))
      }
    }

    "writes" should {
      "passed valid model" when {
        val resJson = Json.parse(
          """
            |{
            |  "obligations": [
            |     {
            |       "typeOfBusiness": "self-employment",
            |       "businessId": "XAIS12345678910",
            |       "obligationDetails": [
            |         {
            |           "periodStartDate": "2019-01-01",
            |           "periodEndDate": "2019-03-31",
            |           "dueDate": "2019-04-30",
            |           "receivedDate": "2019-04-25",
            |           "status": "Fulfilled"
            |         },
            |         {
            |           "periodStartDate": "2019-04-01",
            |           "periodEndDate": "2019-06-30",
            |           "dueDate": "2019-07-31",
            |           "receivedDate": "2019-07-01",
            |           "status": "Fulfilled"
            |         },
            |         {
            |           "periodStartDate": "2019-07-01",
            |           "periodEndDate": "2019-09-30",
            |           "dueDate": "2019-10-31",
            |           "receivedDate": "2019-10-08",
            |           "status": "Fulfilled"
            |         },
            |         {
            |           "periodStartDate": "2019-10-01",
            |           "periodEndDate": "2019-12-31",
            |           "dueDate": "2020-01-31",
            |           "status": "Open"
            |         }
            |       ]
            |    }
            |  ]
            |}
            |""".stripMargin
        )
        "return valid json" in {
          val responseBody = RetrieveEOPSObligationsResponse(Seq(
            Obligation(MtdBusiness.`self-employment`, "XAIS12345678910", Seq(
              ObligationDetail("2019-01-01", "2019-03-31", "2019-04-30", Some("2019-04-25"), MtdStatus.Fulfilled),
              ObligationDetail("2019-04-01", "2019-06-30", "2019-07-31", Some("2019-07-01"), MtdStatus.Fulfilled),
              ObligationDetail("2019-07-01", "2019-09-30", "2019-10-31", Some("2019-10-08"), MtdStatus.Fulfilled),
              ObligationDetail("2019-10-01", "2019-12-31", "2020-01-31", None, MtdStatus.Open)
            ))
          ))

          Json.toJson(responseBody) shouldBe resJson
        }
      }
    }
  }
}
