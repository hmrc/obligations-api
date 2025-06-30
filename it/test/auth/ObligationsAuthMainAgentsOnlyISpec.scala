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

package auth

import api.services.DownstreamStub
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}

class ObligationsAuthMainAgentsOnlyISpec extends AuthMainAgentsOnlyISpec {

  override val callingApiVersion = "2.0"

  override val supportingAgentsNotAllowedEndpoint = "retrieve-periodic-obligations"

  override val mtdUrl = s"/$nino/income-and-expenditure"

  override def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  override val downstreamUri: String = s"/enterprise/obligation-data/nino/$nino/ITSA"

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  override val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse(
      """
        |{
        |    "obligations": [
        |        {
        |            "identification": {
        |                "incomeSourceType": "ITSB",
        |                "referenceNumber": "XAIS12345678901",
        |                "referenceType": "MTDBIS"
        |            },
        |            "obligationDetails": [
        |                {
        |                    "status": "O",
        |                    "inboundCorrespondenceFromDate": "2019-01-01",
        |                    "inboundCorrespondenceToDate": "2019-06-06",
        |                    "inboundCorrespondenceDueDate": "2019-04-30",
        |                    "periodKey": "#001"
        |                }
        |            ]
        |        }
        |    ]
        |}
        |""".stripMargin
    )
  )

}
