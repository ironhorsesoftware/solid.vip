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

import models.{Profile, Project}

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

    val root = (json \ "data" \ "viewer").as[JsValue]

    val login = (root \ "login").as[String]

    models.Profile(
        loginInfo = LoginInfo(SolidGitHubProvider.ID, login),
        name = (root \ "name").as[String],
        picture = (root \ "avatarUrl").asOpt[String].filter(item => !item.isEmpty),
        title = (root \ "company").asOpt[String].filter(item => !item.isEmpty),
        summary = (root \ "bio").asOpt[String].filter(item => !item.isEmpty),
        location = (root \ "location").asOpt[String].filter(item => !item.isEmpty),
        email = (root \ "email").asOpt[String].filter(item => !item.isEmpty),
        website = (root \ "websiteUrl").asOpt[String].filter(item => !item.isEmpty),
        twitterUrl = None,
        gitHubUrl = (root \ "url").asOpt[String].filter(item => !item.isEmpty),
        Some(login),
        projects = getProjects(root),
        workExperience = List()
    )
  }

  def getProjects(json : JsValue) : List[Project] = {
    val projects =
      (json \ "repositories" \ "nodes").as[JsArray].value.map { repository =>
        val thumbnailOpt =
          if ((repository \ "usesCustomOpenGraphImage").as[JsBoolean].value) {
            (repository \ "openGraphImageUrl").asOpt[String]
          } else {
            None
          }

        Project(
            title = (repository \ "name").as[String],
            description = (repository \ "description").asOpt[String].getOrElse(""),
            link = (repository \ "url").as[String],
            thumbnail = thumbnailOpt,
            isContributedTo = (repository \ "isFork").as[JsBoolean].value
        )
      }

    val contributions =
      (json \ "repositoriesContributedTo" \ "nodes").as[JsArray].value.map { repository =>
        Project(
            title = (repository \ "name").as[String],
            description = (repository \ "description").asOpt[String].getOrElse(""),
            link = (repository \ "url").as[String],
            thumbnail = (repository \ "openGraphImageUrl").asOpt[String],
            isContributedTo = true
        )
      }

    (projects ++ contributions).toList
  }
}
