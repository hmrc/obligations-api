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

package v1.models.response.retrieveCrystallisationObligations.des

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.domain.status.{DesStatus, MtdStatus}
import v1.models.response.retrieveCrystallisationObligations.RetrieveCrystallisationObligationsResponse

class DesObligationSpec extends UnitSpec {
  "reads" should {
    "read to a model" when {
      "passed JSON with status F" in {
        val json = Json.parse(
          """
            |{
            |    "status": "F",
            |    "inboundCorrespondenceFromDate": "2018-04-06",
            |    "inboundCorrespondenceToDate": "2019-04-05",
            |    "inboundCorrespondenceDateReceived": "2020-01-25",
            |    "inboundCorrespondenceDueDate": "1920-01-31",
            |    "periodKey": "ITSA"
            |}
            |""".stripMargin)
        val model = DesObligation("2018-04-06", "2019-04-05", "1920-01-31", DesStatus.F, Some("2020-01-25"), "ITSA")

        json.as[DesObligation] shouldBe model
      }
      "passed JSON with status O" in {
        val json = Json.parse(
          """
            |{
            |    "status": "O",
            |    "inboundCorrespondenceFromDate": "2018-04-06",
            |    "inboundCorrespondenceToDate": "2019-04-05",
            |    "inboundCorrespondenceDateReceived": "2020-01-25",
            |    "inboundCorrespondenceDueDate": "1920-01-31",
            |    "periodKey": "ITSA"
            |}
            |""".stripMargin)
        val model = DesObligation("2018-04-06", "2019-04-05", "1920-01-31", DesStatus.O, Some("2020-01-25"), "ITSA")

        json.as[DesObligation] shouldBe model
      }
      "passed JSON with no inboundCorrespondenceDateReceived" in {
        val json = Json.parse(
          """
            |{
            |    "status": "O",
            |    "inboundCorrespondenceFromDate": "2018-04-06",
            |    "inboundCorrespondenceToDate": "2019-04-05",
            |    "inboundCorrespondenceDueDate": "1920-01-31",
            |    "periodKey": "ITSA"
            |}
            |""".stripMargin)
        val model = DesObligation("2018-04-06", "2019-04-05", "1920-01-31", DesStatus.O, None, "ITSA")

        json.as[DesObligation] shouldBe model
      }
    }
  }
  "toMtd" should {
    "return a RetrieveCrystallisationObligationsResponse model" when {
      "passed a valid model" in {
        val model = DesObligation("2018-04-06", "2019-04-05", "1920-01-31", DesStatus.O, None, "ITSA")

        model.toMtd shouldBe RetrieveCrystallisationObligationsResponse("2018-04-06", "2019-04-05", "1920-01-31", MtdStatus.Open, None)
      }
    }
  }
}
