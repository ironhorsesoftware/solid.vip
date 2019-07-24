package controllers

import javax.inject._

import scala.concurrent.{Future, ExecutionContext}

import play.api.{Logging, Configuration}
import play.api.http.HttpEntity
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc.{ControllerComponents, AbstractController, AnyContent, Request}

import com.mohiva.play.silhouette.api.{Silhouette, AuthInfo}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.{SocialProvider, SocialProviderRegistry, CommonSocialProfileBuilder, CommonSocialProfile}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Info, OAuth2Provider}
import com.mohiva.play.silhouette.impl.providers.oauth1.{TwitterProvider}
import com.mohiva.play.silhouette.impl.providers.oauth2.{LinkedInProvider, FacebookProvider}

import models.Member
import modules.InteractiveEnv
import services.SolidGitHubProvider

class ProfileBuilderController @Inject()
    (cc: ControllerComponents, silhouette : Silhouette[InteractiveEnv], socialProviderRegistry: SocialProviderRegistry, ws : WSClient, config : Configuration)
    (implicit assets: AssetsFinder, ex: ExecutionContext)
    extends AbstractController(cc) with I18nSupport with Logging { 

  def view = silhouette.SecuredAction { implicit request : SecuredRequest[InteractiveEnv, AnyContent] =>
    Ok(views.html.profileBuilder(request.identity, socialProviderRegistry))
  }

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String) = silhouette.SecuredAction.async { implicit request: SecuredRequest[InteractiveEnv, AnyContent] =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            result <- collectProfile(p.id, authInfo, profile)
          } yield {
            Ok(result)
            //Ok(views.html.profileBuilder(request.identity, socialProviderRegistry))
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.SignInController.view()).flashing("error" -> Messages("could.not.authenticate"))
    }
  }

  def collectProfile(provider : String, authInfo : AuthInfo, profile : CommonSocialProfile) : Future[JsValue] = {
    logger.info(s"Collecting profile for provider ${provider}, authInfo = ${authInfo} | profile: ${profile}")

    if (provider == SolidGitHubProvider.ID) {
      collectGitHubProfile(authInfo.asInstanceOf[OAuth2Info], profile)
    } else {
      Future.successful(Json.obj(
          "provider" -> JsString(provider),
          "authInfo" -> JsString(authInfo.toString),
          "profile" -> Json.obj(
              "loginInfo" -> Json.obj(
                  "providerId" -> profile.loginInfo.providerID,
                  "providerKey" -> profile.loginInfo.providerKey),
              "fuillNname" -> profile.fullName,
              "firstName" -> profile.firstName,
              "lastName" -> profile.lastName,
              "avatarUrl" -> profile.avatarURL,
              "email" -> profile.email)
      ))
    }
  }

  def buildGitHubQuery(login : String) : String = {
    val query =
s"""
query {
  user(login:"${login}") {
    name
    avatarUrl
    bio
    bioHTML
    company
    login
    location
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

  def collectGitHubProfile(authInfo : OAuth2Info, profile : CommonSocialProfile) : Future[JsValue] = {
    val request =
      ws.url(config.get[String]("github.api.endpoint")).addHttpHeaders("Authorization" -> s"bearer ${authInfo.accessToken}")

    val query = buildGitHubQuery(profile.loginInfo.providerKey)

    logger.info("Sending GitHub request: " + query)
    request.post(query).map { response =>
      logger.info(response.body)
      response.json
    }
  }
}