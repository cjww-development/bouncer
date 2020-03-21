/*
 * Copyright 2020 CJWW Development
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

package com.cjwwdev.bouncer.filters

import com.cjwwdev.bouncer.models.HeaderError
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.Future

trait HeaderVerificationFilter extends Filter {

  val headersToValidate: Map[String, String => Option[HeaderError]]

  val invalidHeaderResponse: (RequestHeader, Seq[HeaderError]) => Future[Result]

  private val nonExistentHeader: String => HeaderError = hk => HeaderError(
    header = hk,
    error = s"This request hasn't specified the $hk header"
  )

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    val validated = headersToValidate
      .flatMap({ case (hk, f) => rh.headers.get(hk).fold[Option[HeaderError]](Some(nonExistentHeader(hk)))(f) })
      .toSeq

    if(validated.isEmpty) f(rh) else invalidHeaderResponse(rh, validated)
  }
}
