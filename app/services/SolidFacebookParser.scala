package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{SocialProfileParser, OAuth2Info}

import play.api.Logging
import play.api.libs.json.{JsValue, JsArray, JsNumber}

import models.Profile

class SolidFacebookParser extends SocialProfileParser[JsValue, Profile, OAuth2Info] with Logging {
  override def parse(json : JsValue, authInfo : OAuth2Info) : Future[Profile] = Future.successful {
    logger.info(s"JSON: ${json}")

    Profile(
        loginInfo = LoginInfo(SolidFacebookProvider.ID, (json \ "id").as[String]),
        name = (json \ "name").as[String],
        picture = (json \ "picture" \ "data" \ "url").asOpt[String].filter(item => !item.isEmpty),
        title = None,
        summary = None,
        location = None,
        email = (json \ "email").asOpt[String].filter(item => !item.isEmpty),
        website = None,
        twitterUrl = None,
        gitHubUrl = None,
        gitHubUsername = None,
        projects = List(),
        workExperience = List()
    )
  }
}
