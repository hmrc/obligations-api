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

package v2.fixtures

import api.models.domain.status.{ DesStatus, MtdStatus }
import play.api.libs.json.{ JsValue, Json }
import v2.models.response.retrieveCrystallisationObligations.Obligation
import v2.models.response.retrieveCrystallisationObligations.des.DesObligation

object RetrieveCrystallisationObligationsFixtures {

  def desObligationModel(status: DesStatus = DesStatus.F, receivedDate: Option[String] = Some("2020-01-25")): DesObligation = DesObligation(
    inboundCorrespondenceFromDate = "2018-04-06",
    inboundCorrespondenceToDate = "2019-04-05",
    inboundCorrespondenceDueDate = "1920-01-31",
    status = status,
    inboundCorrespondenceDateReceived = receivedDate
  )

  def mtdObligationModel(status: MtdStatus = MtdStatus.Fulfilled, receivedDate: Option[String] = Some("2020-01-25")): Obligation = Obligation(
    periodStartDate = "2018-04-06",
    periodEndDate = "2019-04-05",
    dueDate = "1920-01-31",
    status = status,
    receivedDate = receivedDate
  )

  def mtdObligationJson(status: String = "Fulfilled"): JsValue = Json.parse(s"""
      |{
      |    "periodStartDate": "2018-04-06",
      |    "periodEndDate": "2019-04-05",
      |    "dueDate": "1920-01-31",
      |    "status": "$status",
      |    "receivedDate": "2020-01-25"
      |}
      |""".stripMargin)

  val mtdObligationJsonNoReceivedDate: JsValue = Json.parse(s"""
       |{
       |    "periodStartDate": "2018-04-06",
       |    "periodEndDate": "2019-04-05",
       |    "dueDate": "1920-01-31",
       |    "status": "Fulfilled"
       |}
       |""".stripMargin)

}
