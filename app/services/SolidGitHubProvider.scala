package services

import scala.concurrent.Future

import play.api.libs.json.Json

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, CommonSocialProfile, CommonSocialProfileBuilder }
import com.mohiva.play.silhouette.impl.providers.{OAuth2Settings, OAuth2Info}
import com.mohiva.play.silhouette.impl.providers.oauth2.{BaseGitHubProvider, GitHubProvider}
import com.mohiva.play.silhouette.impl.providers.oauth2.GitHubProvider.SpecifiedProfileError


class SolidGitHubProvider (
    protected val httpLayer : HTTPLayer,
    protected val stateHandler : SocialStateHandler,
    val settings : OAuth2Settings) extends BaseGitHubProvider with SolidProfileBuilder {

  override type Self = SolidGitHubProvider

  override val profileParser = new SolidGitHubProfileParser

  override def withSettings(f : (Settings) => Settings) = {
    new SolidGitHubProvider(httpLayer, stateHandler, f(settings))
  }

  override protected def buildProfile(authInfo : OAuth2Info) : Future[Profile] = {
    buildProfileV2(authInfo)
  }

  private def buildProfileV2(authInfo : OAuth2Info) : Future[Profile] = {
    val apiUrl = urls("api")

    logger.info(s"Connecting to $apiUrl with access token ${authInfo.accessToken}")
    httpLayer.url(urls(apiUrl)).withHttpHeaders(("Authorization", s"bearer ${authInfo.accessToken}")).post[String](SolidGitHubProvider.BODY).flatMap { response =>
      val json = response.json
      (json \ "message").asOpt[String] match {
        case Some(msg) =>
          val docURL = (json \ "documentation_url").asOpt[String]

          throw new ProfileRetrievalException(SpecifiedProfileError.format(id, msg, docURL))
        case _ => profileParser.parse(json, authInfo)
      }
    }
  }
}

object SolidGitHubProvider {
  /**
   * The error messages.
   */
  val SpecifiedProfileError = GitHubProvider.SpecifiedProfileError

  /**
   * The GitHub constants.
   */
  val ID = GitHubProvider.ID
  val API = "https://api.github.com/graphql"

  def BODY = {
    val query =
"""
query {
  viewer {
    name
    avatarUrl
    bio
    bioHTML
    company
    login
    location
    email
    url
    websiteUrl
    organizations(first: 10) {
      nodes {
        name
        url
        login
      }
      totalCount
    }
    projects(first: 10) {
      nodes {
        name
        url
      }
    }
    repositories(first: 10, privacy:PUBLIC) {
      nodes {
        name
        owner {
          login
          url
        }
        url
        isFork
      }
      totalCount
    }
    repositoriesContributedTo(first: 10, privacy:PUBLIC) {
      nodes {
        name
        owner {
          login
          id
        }
        isFork
      }
      totalCount
    }
  }
}
  """    

    val jsonQuery = Json.obj(
        "query" -> query
    )

    jsonQuery.toString
  }
}