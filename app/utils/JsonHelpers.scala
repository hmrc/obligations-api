
package utils

import play.api.libs.json.{JsPath, JsResult, JsValue, Json, Reads}

trait JsonHelpers {
  implicit class JsPathOps(jsPath: JsPath) {
    /*  Json Reads that replaces the standard reads for a sequence of type T. Instead of immediately reading in the json
          this takes the raw json sequence and filters out all elements which do not include the required matching element.
          After the filter it executes the standard json reads for the type T to read in only the filtered values.
       */
    def filteredArrayReads[T](filterName: String, matching: String)(implicit rds: Reads[Seq[T]]): Reads[Seq[T]] = (json: JsValue) => {
      json
        .validate[Seq[JsValue]]
        .flatMap(
          readJson =>
            Json
              .toJson(readJson.filter { element =>
                element.\(filterName).asOpt[String].contains(matching)
              })
              .validate[Seq[T]])
    }
  }
}

object JsonHelpers extends JsonHelpers