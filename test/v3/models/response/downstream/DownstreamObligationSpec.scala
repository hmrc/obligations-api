/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.models.response.downstream

import api.models.domain.status.DesStatusV3
import play.api.libs.json.Json
import support.UnitSpec

class DownstreamObligationSpec extends UnitSpec with DownstreamObligationsFixture {

  "DownstreamObligation" when {
    "read from JSON" must {
      "work when all fields are present" in {
        Json
          .parse("""
              |{
              |  "identification": {
              |    "referenceNumber": "refNo",
              |    "referenceType": "refType"
              |  },
              |  "obligationDetails": [
              |    {
              |      "status": "O",
              |      "inboundCorrespondenceFromDate": "2000-01-01",
              |      "inboundCorrespondenceToDate": "2000-02-02",
              |      "inboundCorrespondenceDueDate": "2000-04-04",
              |      "periodKey": "key"
              |    }
              |  ]
              |}""".stripMargin)
          .as[DownstreamObligation] shouldBe
          DownstreamObligation(
            identification = Some(
              DownstreamIdentification(
                incomeSourceType = None,
                referenceNumber = "refNo",
                referenceType = "refType"
              )),
            obligationDetails = Seq(
              DownstreamObligationDetail(
                status = DesStatusV3.O,
                inboundCorrespondenceFromDate = "2000-01-01",
                inboundCorrespondenceToDate = "2000-02-02",
                inboundCorrespondenceDateReceived = None,
                inboundCorrespondenceDueDate = "2000-04-04",
                periodKey = "key"
              ))
          )
      }

      "work when optional fields are absent" in {
        Json
          .parse("""
                   |{
                   |  "obligationDetails": [
                   |    {
                   |      "status": "O",
                   |      "inboundCorrespondenceFromDate": "2000-01-01",
                   |      "inboundCorrespondenceToDate": "2000-02-02",
                   |      "inboundCorrespondenceDueDate": "2000-04-04",
                   |      "periodKey": "key"
                   |    }
                   |  ]
                   |}""".stripMargin)
          .as[DownstreamObligation] shouldBe
          DownstreamObligation(
            identification = None,
            obligationDetails = Seq(
              DownstreamObligationDetail(
                status = DesStatusV3.O,
                inboundCorrespondenceFromDate = "2000-01-01",
                inboundCorrespondenceToDate = "2000-02-02",
                inboundCorrespondenceDateReceived = None,
                inboundCorrespondenceDueDate = "2000-04-04",
                periodKey = "key"
              ))
          )
      }
    }

    "filter" should {
      "remove unwanted ObligationDetails entries" in {
        downstreamObligation(
          None,
          obligationDetails = Seq(
            downstreamObligationDetail(periodKey = "A"),
            downstreamObligationDetail(periodKey = "B")
          )).filterObligationDetails { detail => detail.periodKey == "B" } shouldBe
          downstreamObligation(
            None,
            obligationDetails = Seq(
              downstreamObligationDetail(periodKey = "B")
            ))
      }
    }
  }

}
