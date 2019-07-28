package services

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Logging
import play.api.libs.json._

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.impl.providers.{SocialProvider, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, CommonSocialProfile}
import com.mohiva.play.silhouette.impl.providers.SocialProfileParser
import com.mohiva.play.silhouette.impl.providers.oauth2.GitHubProfileParser

class SolidGitHubProfileParser extends SocialProfileParser[JsValue, CommonSocialProfile, OAuth2Info] with Logging {

  val commonParser = new GitHubProfileParser

  def parse(json: JsValue, authInfo: OAuth2Info) = {
    logger.debug(s"json: ${json} | authInfo: ${authInfo}")
    commonParser.parse(json, authInfo).map { profile =>
      profile.copy(loginInfo = LoginInfo(SolidGitHubProvider.ID, (json \ "login").as[String]))
    }
  }

  def parseV2(json: JsValue, authInfo: OAuth2Info) : models.Profile = {
    logger.debug(s"json: ${json} | authInfo: ${authInfo}")

    val login = (json \ "login").as[String]

    val summary = (json \ "bio").asOpt[String]
    val summaryHtml = (json \ "bioHTML").asOpt[String]

    models.Profile(
        loginInfo = LoginInfo(SolidGitHubProvider.ID, login),
        name = (json \ "name").as[String],
        picture = (json \ "avatarUrl").asOpt[String],
        title = (json \ "company").asOpt[String],
        summary = None, // TODO
        location = (json \ "location").asOpt[String],
        email = (json \ "email").asOpt[String],
        website = (json \ "websiteUrl").asOpt[String],
        twitterUrl = None,
        gitHubUrl = (json \ "url").asOpt[String],
        Some(login),
        projects = List(), // TODO
        workExperience = List()
    )
  }
}
