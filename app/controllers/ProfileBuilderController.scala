package controllers

import javax.inject._

import play.api.{Logging}
import play.api.i18n.I18nSupport
import play.api.mvc.{ControllerComponents, AbstractController, AnyContent}

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry

import models.Member
import modules.InteractiveEnv

class ProfileBuilderController @Inject() (cc: ControllerComponents, silhouette : Silhouette[InteractiveEnv], socialProviderRegistry: SocialProviderRegistry) extends AbstractController(cc) with I18nSupport with Logging { 

  def view = silhouette.SecuredAction { implicit request : SecuredRequest[InteractiveEnv, AnyContent] =>
    Ok(views.html.profileBuilder(request.identity, socialProviderRegistry))
  }
}