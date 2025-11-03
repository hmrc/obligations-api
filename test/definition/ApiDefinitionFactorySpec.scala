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

package definition

import api.mocks.MockHttpClient
import cats.implicits.catsSyntaxValidatedId
import config.Deprecation.NotDeprecated
import config.MockAppConfig
import definition.APIStatus.{ALPHA, BETA}
import routing.Version3
import support.UnitSpec

class ApiDefinitionFactorySpec extends UnitSpec {

  class Test extends MockHttpClient with MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockedAppConfig.apiGatewayContext returns "other/deductions"
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class when confidence level 200 checking is enforced" in {
        testDefinition()
      }

      "return a valid Definition case class when confidence level checking 50 is enforced" in {
        testDefinition()
      }

      def testDefinition(): Unit = new Test {
        Seq(Version3).foreach { version =>
          MockedAppConfig.apiStatus(version) returns "ALPHA"
          MockedAppConfig.endpointsEnabled(version) returns true
          MockedAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
        }

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Obligations (MTD)",
              description = "An API for providing obligations data",
              context = "other/deductions",
              categories = Seq("INCOME_TAX_MTD"),
              versions = Seq(
                APIVersion(
                  version = Version3,
                  status = ALPHA,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      "return the correct status" in new Test {
        MockedAppConfig.apiStatus(Version3) returns "BETA"
        MockedAppConfig
          .deprecationFor(Version3)
          .returns(NotDeprecated.valid)
          .anyNumberOfTimes()
        apiDefinitionFactory.buildAPIStatus(version = Version3) shouldBe BETA
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      "default to alpha" in new Test {
        MockedAppConfig.apiStatus(Version3) returns "ALPHO"
        MockedAppConfig
          .deprecationFor(Version3)
          .returns(NotDeprecated.valid)
          .anyNumberOfTimes()
        apiDefinitionFactory.buildAPIStatus(version = Version3) shouldBe ALPHA
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      Seq(Version3).foreach { version =>
        s"throw exception for $version" in new Test {
          MockedAppConfig.apiStatus(version) returns "DEPRECATED"
          MockedAppConfig
            .deprecationFor(version)
            .returns(s"deprecatedOn date is required for a deprecated version $version".invalid)
            .anyNumberOfTimes()

          val exception: Exception = intercept[Exception] {
            apiDefinitionFactory.buildAPIStatus(version)
          }

          val exceptionMessage: String = exception.getMessage
          exceptionMessage shouldBe s"deprecatedOn date is required for a deprecated version $version"
        }
      }
    }

  }

}
