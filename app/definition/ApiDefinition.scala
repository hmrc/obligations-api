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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import routing.Version
import utils.enums.Enums

enum APIStatus {
  case ALPHA, BETA, STABLE, DEPRECATED, RETIRED
}

case class PublishingException(message: String) extends Exception(message)

object APIStatus {
  val parser: PartialFunction[String, APIStatus] = Enums.parser(values)

  given Format[APIStatus] = Enums.format(values)
}

case class APIVersion(version: Version, status: APIStatus, endpointsEnabled: Boolean)

object APIVersion {

  implicit val readsAPIVersion: Reads[APIVersion] = (
    (__ \ "version").read[Version] and
      (__ \ "status").read[APIStatus] and
      (__ \ "endpointsEnabled").read[Boolean]
  )(APIVersion.apply)

  implicit val writesAPIVersion: OWrites[APIVersion] = OWrites { v =>
    Json.obj(
      "version"          -> v.version,
      "status"           -> v.status,
      "endpointsEnabled" -> v.endpointsEnabled
    )
  }

  implicit val formatAPIVersion: OFormat[APIVersion] =
    OFormat(readsAPIVersion, writesAPIVersion)

}

case class APIDefinition(name: String,
                         description: String,
                         context: String,
                         categories: Seq[String],
                         versions: Seq[APIVersion],
                         requiresTrust: Option[Boolean]) {

  require(name.nonEmpty, "name is required")
  require(context.nonEmpty, "context is required")
  require(categories.nonEmpty, "at least one category is required")
  require(description.nonEmpty, "description is required")
  require(versions.nonEmpty, "at least one version is required")
  require(uniqueVersions, "version numbers must be unique")

  private def uniqueVersions = {
    !versions.map(_.version).groupBy(identity).view.mapValues(_.size).exists(_._2 > 1)
  }

}

object APIDefinition {

  implicit val readsAPIDefinition: Reads[APIDefinition] = (
    (__ \ "name").read[String] and
      (__ \ "description").read[String] and
      (__ \ "context").read[String] and
      (__ \ "categories").read[Seq[String]] and
      (__ \ "versions").read[Seq[APIVersion]] and
      (__ \ "requiresTrust").readNullable[Boolean]
  )(APIDefinition.apply)

  implicit val writesAPIDefinition: OWrites[APIDefinition] = OWrites { d =>
    Json.obj(
      "name"        -> d.name,
      "description" -> d.description,
      "context"     -> d.context,
      "categories"  -> d.categories,
      "versions"    -> d.versions
      // requiresTrust is optional and should be omitted when None.
    ) ++ d.requiresTrust.fold(Json.obj())(v => Json.obj("requiresTrust" -> v))
  }

  implicit val formatAPIDefinition: OFormat[APIDefinition] =
    OFormat(readsAPIDefinition, writesAPIDefinition)

}

case class Definition(api: APIDefinition)

object Definition {

  implicit val readsDefinition: Reads[Definition] =
    (__ \ "api").read[APIDefinition].map(Definition.apply)

  implicit val writesDefinition: OWrites[Definition] = OWrites { d =>
    Json.obj("api" -> d.api)
  }

  implicit val formatDefinition: OFormat[Definition] =
    OFormat(readsDefinition, writesDefinition)

}
