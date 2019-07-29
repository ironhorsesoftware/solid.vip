package controllers

import javax.inject._

import scala.concurrent.{Future, ExecutionContext}

import play.api.Logging
import play.api.http.HttpEntity
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json._
import play.api.mvc.{ControllerComponents, AbstractController, AnyContent, Request}

import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette, AuthInfo}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.{SocialProvider, SocialProviderRegistry, CommonSocialProfileBuilder, CommonSocialProfile}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Info, OAuth2Provider}
import com.mohiva.play.silhouette.impl.providers.oauth1.{TwitterProvider}
import com.mohiva.play.silhouette.impl.providers.oauth2.{LinkedInProvider, FacebookProvider}

import daos.ProfileDAO
import models.{Member, Profile}
import modules.InteractiveEnv
import services.{SolidProfileBuilder, SolidGitHubProvider}

class ProfileBuilderController @Inject()
    (cc: ControllerComponents, silhouette : Silhouette[InteractiveEnv], socialProviderRegistry: SocialProviderRegistry, val profileDao : ProfileDAO)
    (implicit assets: AssetsFinder, ex: ExecutionContext)
    extends AbstractController(cc) with I18nSupport with Logging { 

  def view = silhouette.SecuredAction.async { implicit request : SecuredRequest[InteractiveEnv, AnyContent] =>
    profileDao.retrieve(request.identity.loginInfo).map { profileOpt =>
      profileOpt.getOrElse(Profile(request.identity))
    }.map { profile =>
      Ok(views.html.profileBuilder(request.identity, socialProviderRegistry, profile, None))
    }
  }

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String) = silhouette.SecuredAction.async { implicit request: SecuredRequest[InteractiveEnv, AnyContent] =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with SolidProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profileOpt <- profileDao.retrieve(request.identity.loginInfo)
            newProfile <- p.retrieveProfile(authInfo)
          } yield {
            Ok(views.html.profileBuilder(request.identity, socialProviderRegistry, profileOpt.getOrElse(Profile(request.identity)), Some(newProfile)))
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.ProfileBuilderController.view()).flashing("error" -> Messages("could.not.authenticate"))
    }
  }

  def collectProfile(provider : String, authInfo : AuthInfo, profile : models.Profile) : Future[JsValue] = {
    implicit val loginInfoReads = Json.writes[LoginInfo]
    implicit val projectReads = Json.writes[models.Project]
    implicit val workExperienceReads = Json.writes[models.WorkExperience]
    implicit val profileReads = Json.writes[models.Profile]

    logger.info(s"Collecting profile for provider ${provider}, authInfo = ${authInfo} | profile: ${profile}")

    Future.successful(Json.toJson(profile))
  }

  def save = silhouette.SecuredAction.async { implicit request : SecuredRequest[InteractiveEnv, AnyContent] =>
    profileDao.retrieve(request.identity.loginInfo).map { profileOpt =>
      profileOpt.getOrElse(Profile(request.identity))
    }.map { profile =>
      Ok(views.html.profileBuilder(request.identity, socialProviderRegistry, profile, None))
    }
  }
}
