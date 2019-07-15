package controllers

import javax.inject._

import scala.collection.mutable.StringBuilder

import play.api._
import play.api.i18n.I18nSupport
import play.api.libs.mailer.{ Email, MailerClient, AttachmentFile }
import play.api.mvc._

import forms.ContactForm

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, mailerClient: MailerClient) extends AbstractController(cc) with I18nSupport {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    // request.domain -> The domain.  You can get the subdomain from this.
    Ok(views.html.index(ContactForm.form))
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

}
