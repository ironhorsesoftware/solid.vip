package services

import scala.concurrent.Future

import play.api.Logging
import play.api.libs.json._

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import com.mohiva.play.silhouette.impl.providers.{SocialProvider, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, CommonSocialProfile}
import com.mohiva.play.silhouette.impl.providers.SocialProfileParser

import models.Profile

class SolidTwitterProfileParser extends SocialProfileParser[JsValue, Profile, OAuth1Info] with Logging {
  override def parse(json : JsValue, authOinfo: OAuth1Info) : Future[Profile] = {
    logger.info(s"JSON: ${json}")
    Future.failed(new UnsupportedOperationException)
  }
}
