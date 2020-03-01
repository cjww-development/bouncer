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
import com.cjwwdev.bouncer.responses.{AuthorisationResult, Authorised, NotAuthorised}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.Status.FORBIDDEN
import play.api.mvc.Results.Forbidden
import play.api.mvc.{Request, Result}

import scala.concurrent.{Future, ExecutionContext => ExC}

trait Authorisation {
  val authConnector: AuthConnector

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def notAuthorised(implicit req: Request[_]): Future[Result]

  protected def authorised(id: String)(f: CurrentUser => Future[Result])(implicit request: Request[_], ec: ExC): Future[Result] = {
    authConnector.getCurrentUser flatMap { context =>
      mapToAuthResult(id, context) match {
        case Authorised(ac) => f(ac)
        case _ => notAuthorised
      }
    }
  }

  private def mapToAuthResult(id: String, currentUser: Option[CurrentUser])(implicit request: Request[_]): AuthorisationResult = {
    currentUser.fold(unAuthorised)(ac => if(id == ac.id) authed(ac) else unAuthorised)
  }

  private def authed(currentUser: CurrentUser)(implicit request: Request[_]): AuthorisationResult = {
    logger.info(s"[mapToAuthResult]: User authorised as ${currentUser.id}")
    Authorised(currentUser)
  }

  private def unAuthorised(implicit request: Request[_]): AuthorisationResult = {
    logger.warn("[mapToAuthResult]: User not authorised action deemed forbidden")
    NotAuthorised
  }
}
