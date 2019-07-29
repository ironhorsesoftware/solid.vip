package controllers

import javax.inject._

import scala.concurrent.{Future, ExecutionContext}

import scala.collection.mutable.{Map => MutableMap, ListBuffer}

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
      Ok(views.html.profileBuilder(request.identity, socialProviderRegistry, buildOptions(profile, None)))
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
            Ok(views.html.profileBuilder(request.identity, socialProviderRegistry, buildOptions(profileOpt.getOrElse(Profile(request.identity)), Some(newProfile))))
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.ProfileBuilderController.view()).flashing("error" -> Messages("could.not.authenticate"))
    }
  }

  def save = silhouette.SecuredAction.async { implicit request : SecuredRequest[InteractiveEnv, AnyContent] =>
    profileDao.retrieve(request.identity.loginInfo).map { profileOpt =>
      profileOpt.getOrElse(Profile(request.identity))
    }.map { profile =>
      Ok(views.html.profileBuilder(request.identity, socialProviderRegistry, buildOptions(profile, None)))
    }
  }

  def buildOptions(profile : Profile, alternate : Option[Profile]) : Map[String, List[(String, String)]] = {
    val noneOption = ("None" -> "None")

    var nameOptions = ListBuffer(noneOption)
    var pictureOptions = ListBuffer(noneOption)
    var titleOptions = ListBuffer(noneOption)
    var summaryOptions = ListBuffer(noneOption)
    var locationOptions = ListBuffer(noneOption)
    var emailOptions = ListBuffer(noneOption)
    var websiteOptions = ListBuffer(noneOption)
    var twitterUrlOptions = ListBuffer(noneOption) 
    var gitHubUrlOptions = ListBuffer(noneOption)
    var gitHubUsernameOptions = ListBuffer(noneOption)

    nameOptions += (profile.name -> profile.name)
    addOption(pictureOptions, profile.picture)
    addOption(titleOptions, profile.title)
    addOption(summaryOptions, profile.summary)
    addOption(locationOptions, profile.location)
    addOption(emailOptions, profile.email)
    addOption(websiteOptions, profile.website)
    addOption(twitterUrlOptions, profile.twitterUrl)
    addOption(gitHubUrlOptions, profile.gitHubUrl)
    addOption(gitHubUsernameOptions, profile.gitHubUsername)

    alternate.foreach { altProfile =>
      nameOptions += (altProfile.name -> altProfile.name)
      addOption(pictureOptions, altProfile.picture)
      addOption(titleOptions, altProfile.title)
      addOption(summaryOptions, altProfile.summary)
      addOption(locationOptions, altProfile.location)
      addOption(emailOptions, altProfile.email)
      addOption(websiteOptions, altProfile.website)
      addOption(twitterUrlOptions, altProfile.twitterUrl)
      addOption(gitHubUrlOptions, altProfile.gitHubUrl)
      addOption(gitHubUsernameOptions, altProfile.gitHubUsername)
    }

    var mutableMap = MutableMap[String, List[(String, String)]]()
    mutableMap += ("name" -> nameOptions.toList)
    mutableMap += ("picture" -> pictureOptions.toList)
    mutableMap += ("title" -> titleOptions.toList)
    mutableMap += ("summary" -> summaryOptions.toList)
    mutableMap += ("location" -> locationOptions.toList)
    mutableMap += ("email" -> emailOptions.toList)
    mutableMap += ("website" -> websiteOptions.toList)
    mutableMap += ("twitterUrl" -> twitterUrlOptions.toList)
    mutableMap += ("gitHubUrl" -> gitHubUrlOptions.toList)
    mutableMap += ("gitHubUsername" -> gitHubUsernameOptions.toList)

    mutableMap.toMap
  }

  def addOption(list : ListBuffer[(String, String)], itemOpt : Option[String]) {
    itemOpt.foreach(item => list += (item -> item))
  }
}
