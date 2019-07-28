package services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Logging
import play.api.libs.json._

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.impl.providers.{SocialProvider, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, CommonSocialProfile}
import com.mohiva.play.silhouette.impl.providers.SocialProfileParser
import com.mohiva.play.silhouette.impl.providers.oauth2.GitHubProfileParser

import models.Profile

class SolidGitHubProfileParser extends SocialProfileParser[JsValue, Profile, OAuth2Info] with Logging {

  val commonParser = new GitHubProfileParser

  def parseV1(json: JsValue, authInfo: OAuth2Info) = {
    logger.debug(s"json: ${json} | authInfo: ${authInfo}")
    commonParser.parse(json, authInfo).map { profile =>
      profile.copy(loginInfo = LoginInfo(SolidGitHubProvider.ID, (json \ "login").as[String]))
    }
  }

  def parse(json: JsValue, authInfo: OAuth2Info) : Future[models.Profile] = Future.successful {
    logger.info(s"json: ${json} | authInfo: ${authInfo}")

    val root = (json \ "data" \ "viewer")

    val login = (root \ "login").as[String]

    val summary = (root \ "bio").asOpt[String]
    val summaryHtml = (root \ "bioHTML").asOpt[String]

    models.Profile(
        loginInfo = LoginInfo(SolidGitHubProvider.ID, login),
        name = (root \ "name").as[String],
        picture = (root \ "avatarUrl").asOpt[String],
        title = (root \ "company").asOpt[String],
        summary = None, // TODO
        location = (root \ "location").asOpt[String],
        email = (root \ "email").asOpt[String],
        website = (root \ "websiteUrl").asOpt[String],
        twitterUrl = None,
        gitHubUrl = (root \ "url").asOpt[String],
        Some(login),
        projects = List(), // TODO
        workExperience = List()
    )
  }
}
