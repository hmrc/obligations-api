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

class ObligationsAuthSupportingAgentsAllowedISpec extends AuthSupportingAgentsAllowedISpec {

  override val callingApiVersion = "2.0"

  override val supportingAgentsAllowedEndpoint = "retrieve-eops-obligations"

  override val mtdUrl = s"/$nino/crystallisation"

  override def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  override val downstreamUri = s"/enterprise/obligation-data/nino/$nino/ITSA"

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  override val maybeDownstreamResponseJson: Option[JsValue] =
    Some(
      Json.parse("""
                   | {
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
                   |                    "inboundCorrespondenceDueDate": "2020-01-31",
                   |                    "periodKey": "ITSA"
                   |                }
                   |            ]
                   |        }
                   |    ]
                   |}
    """.stripMargin)
    )

}
