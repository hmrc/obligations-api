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

package api.connectors

import api.mocks.MockHttpClient
import com.google.common.base.Charsets
import config.MockAppConfig
import org.scalamock.handlers.CallHandler
import play.api.http.{HeaderNames, MimeTypes, Status}
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

import java.net.URL
import java.util.Base64
import scala.concurrent.{ExecutionContext, Future}

trait ConnectorSpec extends UnitSpec with Status with MimeTypes with HeaderNames {

  lazy val baseUrl                   = "http://test-BaseUrl"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val otherHeaders: Seq[(String, String)] = Seq(
    "Gov-Test-Scenario" -> "DEFAULT",
    "AnotherHeader"     -> "HeaderValue"
  )

  implicit val hc: HeaderCarrier    = HeaderCarrier(otherHeaders = otherHeaders)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val dummyHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("obligations-api")
    )

  val allowedHeaders: Seq[String] = List(
    "Accept",
    "Gov-Test-Scenario",
    "Content-Type",
    "Location",
    "X-Request-Timestamp",
    "X-Session-Id"
  )

  protected trait ConnectorTest extends MockHttpClient with MockAppConfig {
    protected val baseUrl: String = "http://test-BaseUrl"

    implicit protected val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

    protected val requiredHeaders: Seq[(String, String)]

    protected def willGet[T](url: URL, parameters: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      MockedHttpClient
        .get(
          url = url,
          config = dummyHeaderCarrierConfig,
          parameters = parameters,
          requiredHeaders = requiredHeaders,
          excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
        )
    }

  }

  protected trait DesTest extends ConnectorTest {
    private val token: String       = "des-token"
    private val environment: String = "des-environment"

    protected val requiredHeaders: Seq[(String, String)] = List(
      "Authorization"     -> s"Bearer $token",
      "Environment"       -> environment,
      "User-Agent"        -> "obligations-api",
      "Gov-Test-Scenario" -> "DEFAULT"
    )

    MockedAppConfig.desBaseUrl returns this.baseUrl
    MockedAppConfig.desToken returns token
    MockedAppConfig.desEnvironment returns environment
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedHeaders)
  }

  protected trait HipTest extends ConnectorTest {
    private val clientId: String     = "clientId"
    private val clientSecret: String = "clientSecret"
    private val environment: String  = "hip-environment"

    private val token: String = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes(Charsets.UTF_8))

    protected val requiredHeaders: Seq[(String, String)] = List(
      "Authorization"     -> s"Basic $token",
      "Environment"       -> environment,
      "User-Agent"        -> "obligations-api",
      "CorrelationId"     -> correlationId,
      "Gov-Test-Scenario" -> "DEFAULT"
    )

    MockedAppConfig.hipBaseUrl returns this.baseUrl
    MockedAppConfig.hipEnv returns environment
    MockedAppConfig.hipClientId returns clientId
    MockedAppConfig.hipClientSecret returns clientSecret
    MockedAppConfig.hipEnvironmentHeaders returns Some(allowedHeaders.filterNot(Set("Content-Type")))
  }

}
