package controllers

import javax.inject._

import scala.concurrent.{Future, ExecutionContext}

import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._

import com.mohiva.play.silhouette.api.{Silhouette, LoginInfo, LogoutEvent}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import daos.ProfileDAO
import modules.InteractiveEnv
import utils.WithProvider

class ExportProfileController @Inject()(
    cc: ControllerComponents,
    silhouette : Silhouette[InteractiveEnv],
    credentialsProvider: CredentialsProvider,
    val profileDao : ProfileDAO)(implicit ex : ExecutionContext) extends AbstractController(cc) with I18nSupport with Logging {

    def view = silhouette.SecuredAction(WithProvider[InteractiveEnv#A](CredentialsProvider.ID)).async { implicit request: SecuredRequest[InteractiveEnv, AnyContent] =>
      profileDao.retrieve(request.identity.loginInfo) map { profileOpt =>
        profileOpt match {
          case Some(profile) => Ok(views.html.exportProfile(request.identity, profile.toTtl))
          case None => Redirect("https://solid.vip")
        }
      }
    }
}