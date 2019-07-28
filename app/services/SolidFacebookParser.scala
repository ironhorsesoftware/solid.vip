package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{SocialProfileParser, OAuth2Info}

import play.api.Logging
import play.api.libs.json.{JsValue, JsArray, JsNumber}

import models.Profile

class SolidFacebookParser extends SocialProfileParser[JsValue, Profile, OAuth2Info] with Logging {
  override def parse(json : JsValue, authInfo : OAuth2Info) : Future[Profile] = {
    logger.info(s"JSON: ${json}")
    Future.failed(new UnsupportedOperationException)
  }
}
