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

package com.cjwwdev.bouncer.frontend

import com.cjwwdev.bouncer.connectors.AuthConnector
import com.cjwwdev.bouncer.models.CurrentUser
import org.slf4j.LoggerFactory
import play.api.mvc._

import scala.concurrent.{Future, ExecutionContext => ExC}

trait AuthorisedAction extends BaseController {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private type AuthorisedAction = Request[AnyContent] => CurrentUser => Future[Result]

  protected val authConnector: AuthConnector
  protected def unauthorisedRedirect: Call

  def isAuthorised(f: => AuthorisedAction)(implicit ec: ExC): Action[AnyContent] = Action.async { implicit request =>
    authConnector.getCurrentUser flatMap {
      case Some(user) =>
        logger.info(s"Authenticated as ${user.id} on ${request.path}")
        f(request)(user)
      case _          =>
        logger.warn(s"Unauthenticated user attempting to access ${request.path}; redirecting to login")
        Action(Redirect(unauthorisedRedirect).withNewSession)(request)
    }
  }
}
