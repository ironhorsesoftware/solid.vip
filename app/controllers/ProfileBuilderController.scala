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
import forms.ProfileForm
import models.{Member, Profile, Project, WorkExperience}
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
      Ok(views.html.profileBuilder(request.identity, socialProviderRegistry, ProfileForm.form.fill(ProfileForm.Data(profile)), buildOptions(profile, None)))
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
            val profile = profileOpt.getOrElse(Profile(request.identity))
            Ok(views.html.profileBuilder(request.identity, socialProviderRegistry, ProfileForm.form.fill(ProfileForm.Data(profile)), buildOptions(profile, Some(newProfile))))
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
    profileDao.retrieve(request.identity.loginInfo).flatMap { existingProfileOpt =>
      ProfileForm.form.bindFromRequest.fold (
        form => Future.successful(BadRequest(views.html.profileBuilder(request.identity, socialProviderRegistry, form, buildOptions(existingProfileOpt.getOrElse(Profile(request.identity)), None)))),
        data => {
          val newProfile = data.toProfile(request.identity.loginInfo)

          val updateResult =
            if (existingProfileOpt.isDefined) {
              profileDao.update(newProfile)
            } else {
              profileDao.create(newProfile)
            }

          updateResult.map { _ =>
            Ok(views.html.profileBuilder(request.identity, socialProviderRegistry, ProfileForm.form.fill(ProfileForm.Data(newProfile)), buildOptions(newProfile, None)))
          }
        }
      )
    }
  }

  def buildOptions(profile : Profile, alternate : Option[Profile]) : Map[String, List[(String, String)]] = {

    var pictureOptions : MutableMap[String, String] = MutableMap()
    var titleOptions : MutableMap[String, String] = MutableMap()
    var summaryOptions : MutableMap[String, String] = MutableMap()
    var locationOptions : MutableMap[String, String] = MutableMap()
    var emailOptions : MutableMap[String, String] = MutableMap()
    var websiteOptions : MutableMap[String, String] = MutableMap()
    var twitterUrlOptions : MutableMap[String, String] = MutableMap() 
    var gitHubUrlOptions : MutableMap[String, String] = MutableMap()
    var gitHubUsernameOptions : MutableMap[String, String] = MutableMap()
    var nameOptions : MutableMap[String, String] = MutableMap()
    var projectOptions : MutableMap[String, String] = MutableMap()
    var workExperienceOptions : MutableMap[String, String] = MutableMap()

    profile.projects.foreach(project => addProject(projectOptions, project))
    profile.workExperience.foreach(workExperience => addWorkExperience(workExperienceOptions, workExperience))

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

      altProfile.projects.foreach (project => addProject(projectOptions, project))
      altProfile.workExperience.foreach(workExperience => addWorkExperience(workExperienceOptions, workExperience))
    }

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

    var mutableMap = MutableMap[String, List[(String, String)]]()
    mutableMap += ("name" -> addNoneOption(nameOptions.toList.sortWith(sortByName)))
    mutableMap += ("picture" -> addNoneOption(pictureOptions.toList.sortWith(sortByName)))
    mutableMap += ("title" -> addNoneOption(titleOptions.toList.sortWith(sortByName)))
    mutableMap += ("summary" -> addNoneOption(summaryOptions.toList.sortWith(sortByName)))
    mutableMap += ("location" -> addNoneOption(locationOptions.toList.sortWith(sortByName)))
    mutableMap += ("email" -> addNoneOption(emailOptions.toList.sortWith(sortByName)))
    mutableMap += ("website" -> addNoneOption(websiteOptions.toList.sortWith(sortByName)))
    mutableMap += ("twitterUrl" -> addNoneOption(twitterUrlOptions.toList.sortWith(sortByName)))
    mutableMap += ("gitHubUrl" -> addNoneOption(gitHubUrlOptions.toList.sortWith(sortByName)))
    mutableMap += ("gitHubUsername" -> addNoneOption(gitHubUsernameOptions.toList.sortWith(sortByName)))
    mutableMap += ("projects" -> projectOptions.toList.sortWith(sortByName))
    mutableMap += ("workExperience" -> workExperienceOptions.toList.sortWith(sortByName))

    mutableMap.toMap
  }

  def addOption(options : MutableMap[String, String], itemOpt : Option[String]) {
    itemOpt.foreach(item => options += (item -> item))
  }

  def addProject(projects : MutableMap[String, String], project : Project) {
    projects += (Json.toJson(project).toString -> project.title)
  }

  def addWorkExperience(experiences : MutableMap[String, String], workExperience : WorkExperience) {
    experiences += (Json.toJson(workExperience).toString -> workExperience.company)
  }

  def sortByName(project1 : (String, String), project2 : (String, String)) = {
    project1._2 < project2._2
  }

  def addNoneOption(options : List[(String, String)]) = {
    options :+ ("None" -> "None")
  }
}
