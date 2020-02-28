package com.cjwwdev.bouncer.models

import play.api.libs.json.{Json, OFormat, OWrites}

class HeaderError(header: String, error: String)

object HeaderError {
  implicit val writer: OWrites[HeaderError] = Json.writes[HeaderError]
}
