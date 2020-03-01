/*
 * Copyright 2018 CJWW Development
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

package com.cjwwdev.bouncer.helpers

import java.net.URI

import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse

trait MockHttpResponse {
  def mockWSResponse(statusCode: Int, bodyInput: JsValue): WSResponse = new WSResponse {
    override def headers: Map[String, Seq[String]]   = ???
    override def bodyAsSource: Source[ByteString, _] = ???
    override def cookie(name: String)                = ???
    override def underlying[T]                       = ???
    override def body                                = ???
    override def bodyAsBytes                         = ???
    override def cookies                             = ???
    override def allHeaders                          = ???
    override def xml                                 = ???
    override def statusText                          = ???
    override def json                                = bodyInput
    override def header(key: String)                 = ???
    override def status                              = statusCode
    override def uri: URI                            = ???
  }
}
