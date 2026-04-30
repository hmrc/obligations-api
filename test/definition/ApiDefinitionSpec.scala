/*
 * Copyright 2026 HM Revenue & Customs
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

package definition

import definition.APIStatus.ALPHA
import play.api.libs.json.Json
import routing.Version3
import support.UnitSpec

class ApiDefinitionSpec extends UnitSpec {

  val apiVersion: APIVersion       = APIVersion(Version3, ALPHA, endpointsEnabled = false)
  val apiDefinition: APIDefinition = APIDefinition("b", "c", "d", Seq("e"), Seq(apiVersion), Some(false))
  val definition: Definition       = Definition(apiDefinition)

  "APIVersion" when {
    "read/write from/to valid JSON" should {
      "produce the expected object" in {
        Json.toJson(apiVersion).as[APIVersion] shouldBe apiVersion
      }
    }
  }

  "Definition" when {
    "read/write from/to valid JSON" should {
      "produce the expected object" in {
        Json.toJson(definition).as[Definition] shouldBe definition
      }
    }
  }

  "APIDefinition" when {
    "read/write from/to valid JSON" should {
      "produce the expected object" in {
        Json.toJson(apiDefinition).as[APIDefinition] shouldBe apiDefinition
      }
    }

    "the 'name' parameter is empty" should {
      "throw an 'IllegalArgumentException'" in {
        assertThrows[IllegalArgumentException](
          apiDefinition.copy(name = "")
        )
      }
    }
  }

  "the 'description' parameter is empty" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(description = "")
      )
    }
  }

  "the 'context' parameter is empty" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(context = "")
      )
    }
  }

  "the 'categories' parameter is empty" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(categories = Seq())
      )
    }
  }

  "the 'versions' parameter is empty" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(versions = Seq(apiVersion, apiVersion))
      )
    }
  }

  "the 'versions' parameter is not unique" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(versions = Seq())
      )
    }
  }

}
