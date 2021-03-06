package controllers

import javax.inject._

import scala.concurrent.{Future, ExecutionContext}

import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc._

import com.mohiva.play.silhouette.api.{Silhouette, LoginInfo, LogoutEvent}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import daos.ProfileDAO
import services.MemberService
import modules.InteractiveEnv
import utils.WithProvider

class DeleteAccountController @Inject()(
    cc: ControllerComponents,
    mailerClient: MailerClient,
    silhouette : Silhouette[InteractiveEnv],
    credentialsProvider: CredentialsProvider,
    authInfoRepository: AuthInfoRepository,
    val memberService : MemberService,
    val profileDao : ProfileDAO)(implicit ex : ExecutionContext) extends AbstractController(cc) with I18nSupport with Logging {

  def view = silhouette.SecuredAction(WithProvider[InteractiveEnv#A](CredentialsProvider.ID)) { implicit request: SecuredRequest[InteractiveEnv, AnyContent] =>
    Ok(views.html.deleteAccount(request.identity))
  }

  def delete = silhouette.SecuredAction(WithProvider[InteractiveEnv#A](CredentialsProvider.ID)).async { implicit request: SecuredRequest[InteractiveEnv, AnyContent] =>
    profileDao.delete(request.identity.loginInfo).flatMap { result =>
      memberService.delete(request.identity).flatMap { nextResult =>
        val result = Redirect(routes.HomeController.index())
        silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
        silhouette.env.authenticatorService.discard(request.authenticator, result)
      }
    }
  }
}
