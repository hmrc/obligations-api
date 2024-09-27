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

package v3.retrieveCrystallisation.model.response

import api.models.domain.status.MtdStatusV3
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v3.models.response.domain.{ObligationDetail, ObligationsFixture}

class RetrieveCrystallisationObligationsResponseSpec extends UnitSpec with ObligationsFixture {

  private def mtdObligationModel(status: MtdStatusV3 = MtdStatusV3.fulfilled, receivedDate: Option[String] = Some("2020-01-25")): ObligationDetail =
    ObligationDetail(
      periodStartDate = "2018-04-06",
      periodEndDate = "2019-04-05",
      dueDate = "1920-01-31",
      status = status,
      receivedDate = receivedDate
    )

  val mtdObligationJsonNoReceivedDate: JsValue =
    Json.parse(s"""
                  |{
                  |    "periodStartDate": "2018-04-06",
                  |    "periodEndDate": "2019-04-05",
                  |    "dueDate": "1920-01-31",
                  |    "status": "fulfilled"
                  |}
                  |""".stripMargin)

  def mtdObligationJson(status: String = "Fulfilled"): JsValue =
    Json.parse(s"""
                  |{
                  |    "periodStartDate": "2018-04-06",
                  |    "periodEndDate": "2019-04-05",
                  |    "dueDate": "1920-01-31",
                  |    "status": "$status",
                  |    "receivedDate": "2020-01-25"
                  |}
                  |""".stripMargin)

  "writes" should {
    "write to JSON" when {
      "passed a response with multiple obligations" in {
        val model = RetrieveCrystallisationObligationsResponse(
          List(
            mtdObligationModel(status = MtdStatusV3.fulfilled),
            mtdObligationModel(status = MtdStatusV3.open),
            mtdObligationModel(receivedDate = None)
          ))

        Json.toJson(model) shouldBe Json.parse(s"""
             |{
             |    "obligations": [
             |        ${mtdObligationJson("fulfilled")},
             |        ${mtdObligationJson("open")},
             |        $mtdObligationJsonNoReceivedDate
             |    ]
             |}
             |""".stripMargin)
      }
    }
  }

}
