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

import akka.stream.Materializer
import com.cjwwdev.bouncer.models.HeaderError
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, Ok}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class HeaderVerificationFilterSpec extends PlaySpec with GuiceOneAppPerSuite {

  val materializer: Materializer = app.injector.instanceOf[Materializer]

  val testFilter = new HeaderVerificationFilter {
    val appIdHeaderValidator: String => Option[HeaderError] = appId => {
      if(appId.length < 3) Some(HeaderError("X-AppId-Origin", "Header is less than 3 in length, invalid")) else None
    }

    val originHeaderValidator: String => Option[HeaderError] = ip => {
      if(ip != "127.0.0.1") Some(HeaderError("X-Forwarded-For", "Invalid IP address source")) else None
    }

    override val headersToValidate: Map[String, String => Option[HeaderError]] = Map(
      "X-AppId-Origin" -> appIdHeaderValidator,
      "X-Forwarded-For" -> originHeaderValidator
    )

    override val invalidHeaderResponse: Seq[HeaderError] => Future[Result] = errs => {
      val response = Json.obj(
        "status" -> 400,
        "body" -> errs
      )
      Future.successful(BadRequest(response))
    }

    override implicit def mat: Materializer = materializer
  }

  def okFunction: Future[Result] = Future.successful(Ok)

  "HeaderVerificationFilter" should {
    "return an Ok" when {
      "the X-AppId-Origin is more than 3 chars in length" in {
        val req = FakeRequest().withHeaders(
          "X-AppId-Origin" -> "123456789",
          "X-Forwarded-For" -> "127.0.0.1"
        )

        val res = testFilter.apply(_ => okFunction)(req)

        status(res) mustBe OK
      }
    }

    "return a BadRequest" when {
      "the X-AppId-Origin is less than 3 chars in length and the IP isn't localhost" in {
        val req = FakeRequest().withHeaders(
          "X-AppId-Origin" -> "12",
          "X-Forwarded-For" -> "1.1.1.1"
        )

        val res = testFilter.apply(_ => okFunction)(req)

        status(res) mustBe BAD_REQUEST
        contentAsJson(res) mustBe Json.obj(
          "status" -> 400,
          "body" -> Json.arr(
            Json.obj(
              "header" -> "X-AppId-Origin",
              "error" -> "Header is less than 3 in length, invalid"
            ),
            Json.obj(
              "header" -> "X-Forwarded-For",
              "error" -> "Invalid IP address source"
            )
          )
        )
      }

      "the X-AppId-Origin and X-Forwarded-For are not present" in {
        val req = FakeRequest()

        val res = testFilter.apply(_ => okFunction)(req)

        status(res) mustBe BAD_REQUEST
        contentAsJson(res) mustBe Json.obj(
          "status" -> 400,
          "body" -> Json.arr(
            Json.obj(
              "header" -> "X-AppId-Origin",
              "error" -> "This request hasn't specified the X-AppId-Origin header"
            ),
            Json.obj(
              "header" -> "X-Forwarded-For",
              "error" -> "This request hasn't specified the X-Forwarded-For header"
            )
          )
        )
      }
    }
  }
}
