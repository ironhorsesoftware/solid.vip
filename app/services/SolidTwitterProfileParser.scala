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
  override def parse(json : JsValue, authOinfo: OAuth1Info) : Future[Profile] = Future.successful {
    logger.info(s"JSON: ${json}")

    Profile(
        loginInfo = LoginInfo(SolidTwitterProvider.ID, (json \ "id").as[JsNumber].toString),
        name = (json \ "name").as[String],
        picture = (json \ "profile_image_url_https").asOpt[String],
        title = None,
        summary = (json \ "description").asOpt[String],
        location = (json \ "location").asOpt[String],
        email = (json \ "email").asOpt[String],
        website = (json \ "url").asOpt[String],
        twitterUrl = (json \ "screen_name").asOpt[String].map (screenName => s"https://twitter.com/${screenName}"),
        gitHubUrl = None,
        gitHubUsername = None,
        projects = List(),
        workExperience = List()
    )
  }
}
