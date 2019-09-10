package controllers

import javax.inject._

import scala.concurrent.{Future, ExecutionContext}

import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc._

import com.mohiva.play.silhouette.api.{Silhouette, LoginInfo}

import daos.ProfileDAO
import modules.InteractiveEnv


class DeleteAccountController @Inject()(cc: ControllerComponents, mailerClient: MailerClient, silhouette : Silhouette[InteractiveEnv], val profileDao : ProfileDAO)(implicit ex : ExecutionContext) extends AbstractController(cc) with I18nSupport with Logging {
  
}