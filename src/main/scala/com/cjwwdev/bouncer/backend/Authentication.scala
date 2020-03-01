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
package com.cjwwdev.bouncer.backend

import com.cjwwdev.bouncer.connectors.AuthConnector
import com.cjwwdev.bouncer.models.CurrentUser
import com.cjwwdev.bouncer.responses.{Authenticated, AuthorisationResult, NotAuthorised}
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc.{Request, Result}

import scala.concurrent.{Future, ExecutionContext => ExC}

trait Authentication {
  val authConnector: AuthConnector

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def notAuthenticated(implicit req: Request[_]): Future[Result]

  protected def authenticated(id: String)(f: => Future[Result])(implicit ec: ExC, request: Request[_]): Future[Result] = {
    authConnector.getCurrentUser flatMap { context =>
      mapToAuthResult(context) match {
        case Authenticated => f
        case _ => notAuthenticated
      }
    }
  }

  private def mapToAuthResult(currentUser: Option[CurrentUser])(implicit request: Request[_]): AuthorisationResult = {
    currentUser.fold(unAuthorised)(currentUser => authorised(currentUser.id))
  }

  private def authorised(id: String)(implicit request: Request[_]): AuthorisationResult = {
    logger.info(s"[mapToAuthResult]: User authorised as $id")
    Authenticated
  }

  private def unAuthorised(implicit request: Request[_]): AuthorisationResult = {
    logger.warn("[mapToAuthResult]: User not authorised action deemed forbidden")
    NotAuthorised
  }
}
