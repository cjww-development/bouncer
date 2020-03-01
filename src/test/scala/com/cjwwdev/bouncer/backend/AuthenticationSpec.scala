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
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.ArgumentMatchers
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results.{Ok, Forbidden}
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AuthenticationSpec extends PlaySpec with MockitoSugar {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  class Setup extends Authentication {
    override val authConnector: AuthConnector = mockAuthConnector

    override def notAuthenticated(implicit req: Request[_]): Future[Result] = Future.successful(Forbidden)

    def testAuthentication(implicit request: Request[_]): Future[Result] = authenticated("testUserId") {
      Future.successful(Ok)
    }
  }

  val now = new DateTime(DateTimeZone.UTC)

  val testUser = CurrentUser(
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

  "authenticated" should {
    "return an Ok" when {
      "a matching auth context has been found" in new Setup {
        implicit val request = FakeRequest()

        when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testUser)))

        val result = testAuthentication
        status(result) mustBe OK
      }
    }

    "return a forbidden" when {
      "no auth context has been found" in new Setup {
        implicit val request = FakeRequest()

        when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val result = testAuthentication
        status(result) mustBe FORBIDDEN
      }
    }
  }
}
