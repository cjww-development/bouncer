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
import org.mockito.ArgumentMatchers
import play.api.libs.json.Json
import play.api.mvc.Results.{NotFound, Ok}
import play.api.mvc.{Call, ControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when
import org.scalatestplus.play.PlaySpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisedActionSpec extends PlaySpec with MockitoSugar {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val testLoginRedirect: Call = Call("GET", "/")

  val testAuthAction: AuthorisedAction = new AuthorisedAction {
    override val controllerComponents: ControllerComponents = stubControllerComponents()
    override def unauthorisedRedirect: Call = testLoginRedirect
    override val authConnector: AuthConnector = mockAuthConnector
  }

  val testUser: CurrentUser = CurrentUser(
    contextId      = "testContextId",
    id             = "testUserId",
    credentialType = "testTyoe",
    orgDeversityId = Some("testOrgDevId"),
    orgName        = None,
    firstName      = None,
    lastName       = None,
    role           = None,
    enrolments     = None
  )

  def okFunction: Future[Result] = Future.successful(Ok(Json.toJson(testUser)))
  def notFoundFunction: Future[Result] = Future.successful(NotFound)

  "isAuthorised" should {
    "return Some CurrentUser" when {
      "a user has been successfully authenticated" in {

        when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testUser)))

        val result = testAuthAction.isAuthorised { _ => _ =>
          okFunction
        }(global)(FakeRequest())

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(testUser)
      }
    }

    "return None" when {
      "a user has not been successfully authenticated" in {
        when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val result = testAuthAction.isAuthorised { _ => _ =>
          okFunction
        }(global)(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/")
      }
    }
  }
}
