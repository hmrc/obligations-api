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

package routing

import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json.{ JsResultException, JsString }
import play.api.test.FakeRequest
import routing.Version._
import support.UnitSpec

class VersionsSpec extends UnitSpec {

  "Versions" when {
    "retrieved from a request header" must {
      "return the specified V1 version" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))) shouldBe Right(Version1)
      }

      "return the specified V2 version" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.2.0+json"))) shouldBe Right(Version2)
      }

      "return the specified V3 version" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.3.0+json"))) shouldBe Right(Version3)
      }

      "return an error if the version is unsupported" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.4.0+json"))) shouldBe Left(VersionNotFound)
      }

      "return an error if the Accept header value is invalid" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/XYZ.1.0+json"))) shouldBe Left(InvalidHeader)
      }
    }

    "VersionWrites" must {
      "return Version1 when given 1.0" in {
        JsString("1.0").as[Version] shouldBe Version1
      }

      "return Version2 when given 2.0" in {
        JsString("2.0").as[Version] shouldBe Version2
      }

      "return Version3 when given 3.0" in {
        JsString("3.0").as[Version] shouldBe Version3
      }

      "return Version3 when given unrecognised version" in {
        assertThrows[JsResultException] {
          JsString("4.0").as[Version]
        }
      }
    }
  }
}
