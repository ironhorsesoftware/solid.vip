package controllers

import java.net.URLDecoder
import java.util.UUID
import javax.inject.Inject

import daos.MemberDAO
import modules.InteractiveEnv
import services.{AuthTokenService, MemberService}

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.Logging
import play.api.i18n.{ I18nSupport, Messages }
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }


import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Activate Account` controller.
 *
 * @param components       The Play controller components.
 * @param silhouette       The Silhouette stack.
 * @param userService      The user service implementation.
 * @param authTokenService The auth token service implementation.
 * @param mailerClient     The mailer client.
 * @param ex               The execution context.
 */
class ActivateAccountController @Inject() (
  components: ControllerComponents,
  silhouette: Silhouette[InteractiveEnv],
  memberService: MemberService,
  authTokenService: AuthTokenService,
  mailerClient: MailerClient
)(
  implicit
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport with Logging {

  /**
   * Sends an account activation email to the user with the given email.
   *
   * @param email The email address of the user to send the activation mail to.
   * @return The result to display.
   */
  def send(username: String) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    val decodedUsername = URLDecoder.decode(username, "UTF-8")
    val loginInfo = LoginInfo(CredentialsProvider.ID, decodedUsername)
    val result = Redirect(routes.SignInController.view()).flashing("info" -> Messages("activation.email.sent", decodedUsername))

    memberService.retrieve(loginInfo).flatMap {
      case Some(member) if !member.activated =>
        authTokenService.create(member.id).map { authToken =>
          val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()

          mailerClient.send(Email(
            subject = Messages("email.activate.account.subject"),
            from = Messages("email.from"),
            to = Seq(member.email.get),
            bodyText = Some(views.txt.emails.activateAccount(member, url).body),
            bodyHtml = Some(views.html.emails.activateAccount(member, url).body)
          ))
          result
        }
      case None => Future.successful(result)
    }
  }

  /**
   * Activates an account.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def activate(token: UUID) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) => memberService.retrieve(authToken.userID).flatMap {
        case Some(member) if member.loginInfo.providerID == CredentialsProvider.ID =>
          memberService.save(member.copy(activated = true)).map { _ =>
            Redirect(routes.SignInController.view()).flashing("success" -> Messages("account.activated"))
          }
        case _ => Future.successful(Redirect(routes.SignInController.view()).flashing("error" -> Messages("invalid.activation.link")))
      }
      case None => Future.successful(Redirect(routes.SignInController.view()).flashing("error" -> Messages("invalid.activation.link")))
    }
  }
}
