package com.cjwwdev.bouncer.filters

import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.Future

trait HeaderVerificationFilter extends Filter {

  val headersToValidate: Seq[String]

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    
  }
}
