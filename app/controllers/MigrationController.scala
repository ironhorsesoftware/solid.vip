package controllers

import javax.inject._

import scala.concurrent.{Future, ExecutionContext}

import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._

import com.mohiva.play.silhouette.api.{Silhouette, LoginInfo, LogoutEvent}
import com.mohiva.play.silhouette.api.actions.UserAwareRequest
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import daos.mongodb.MongoProfileDAO
import daos.slick.SlickProfileDAO
import models.Profile
import modules.InteractiveEnv
import utils.WithProvider

class MigrationController @Inject()(
    cc: ControllerComponents,
    silhouette : Silhouette[InteractiveEnv],
    credentialsProvider: CredentialsProvider,
    mongoProfileDao : MongoProfileDAO,
    slickProfileDao : SlickProfileDAO) (implicit ex : ExecutionContext) extends AbstractController(cc) with I18nSupport with Logging {
  
    def migrate = silhouette.UserAwareAction.async { implicit request : UserAwareRequest[InteractiveEnv, AnyContent] =>
        Future.successful(Ok("Migrated Successfully"))
    }
}
