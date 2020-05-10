package controllers

import javax.inject._

import scala.collection.mutable.StringBuilder
import scala.concurrent.{Future, ExecutionContext}

import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc._

import com.mohiva.play.silhouette.api.{Silhouette, LoginInfo}
import com.mohiva.play.silhouette.api.actions.UserAwareRequest
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import daos.ProfileDAO
import forms.ContactForm
import models.Member
import modules.InteractiveEnv

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, mailerClient: MailerClient, silhouette : Silhouette[InteractiveEnv], val profileDao : ProfileDAO)(implicit ex : ExecutionContext) extends AbstractController(cc) with I18nSupport with Logging {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = silhouette.UserAwareAction.async { implicit request : UserAwareRequest[InteractiveEnv, AnyContent] =>
    // request.domain -> The domain.  You can get the subdomain from this.
    logger.info(s"Hostname: ${request.domain}")
    val domainSplit = request.domain.split('.')

    if (domainSplit.length == 4) {
      logger.info(s"Fetching domain for username ${domainSplit.head}")
      handleProfilePage(domainSplit.head)
    } else {
      Future.successful(Ok(views.html.index(ContactForm.form, request.identity)))
    }
  }

  def contact = Action { implicit request => 
    ContactForm.form.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.index(formWithErrors)),
        data => {
          val msgBuilder = new StringBuilder(data.name)
          msgBuilder.append("\nE-Mail: ").append(data.email)
          msgBuilder.append("\n\n").append(data.content)

          mailerClient.send(Email(
              subject = data.subject,
              from = "admin@solid.vip",
              to = Seq("mpigott@ironhorsesoftware.com"),
              replyTo = Seq(data.email),
              bodyText = Some(msgBuilder.toString)))
          Redirect(routes.HomeController.index).flashing("msg" -> "Thank you for your interest!  Your e-mail has been sent.")
        }
    )
  }

  def profile(username : String) = silhouette.UserAwareAction.async { implicit request : UserAwareRequest[InteractiveEnv, AnyContent] =>
    handleProfilePage(username)
  }

  def handleProfilePage(username : String)(implicit request : UserAwareRequest[InteractiveEnv, AnyContent]): Future[Result] = {
    profileDao.retrieve(LoginInfo(CredentialsProvider.ID, username)).map { profileOpt =>
      if (profileOpt.isDefined) {
        Ok(views.html.profile.profile(profileOpt.get))
      } else {
        Redirect("https://solid-vip.herokuapp.com")
      }
    }
  }
}
