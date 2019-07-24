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

class ExtendedGitHubProfileParser extends SocialProfileParser[JsValue, CommonSocialProfile, OAuth2Info] with Logging {

  val commonParser = new GitHubProfileParser

  def parse(json: JsValue, authInfo: OAuth2Info) = {
    logger.info(s"json: ${json} | authInfo: ${authInfo}")
    commonParser.parse(json, authInfo).map { profile =>
      profile.copy(loginInfo = LoginInfo(ExtendedGitHubProvider.ID, (json \ "login").as[String]))
    }
  }
}
