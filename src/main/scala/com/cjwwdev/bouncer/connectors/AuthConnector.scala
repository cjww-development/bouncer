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

package com.cjwwdev.bouncer.connectors

import com.cjwwdev.bouncer.models.CurrentUser
import play.api.mvc.Request

import scala.concurrent.{Future, ExecutionContext => ExC}

trait AuthConnector {

  type User = Option[CurrentUser]

  def getCurrentUser(implicit ec: ExC, request: Request[_]): Future[User]

  def consultSessionStore(cookieId: String)(f: String => Future[User])(implicit ec: ExC, req: Request[_]): Future[User]

  def consultAuth(contextId: String)(implicit ec: ExC, req: Request[_]): Future[User]
}
