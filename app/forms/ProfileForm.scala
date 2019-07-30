package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._

import com.mohiva.play.silhouette.api.LoginInfo

import models.{Profile, Project, WorkExperience}

object ProfileForm {

    val NO_VALUE = "None"

    case class Data(
      name : String,
      picture : String,
      title : String,
      summary : String,
      location : String,
      email : String,
      website : String,
      twitterUrl : String,
      gitHubUrl : String,
      gitHubUsername : String,
      projects : Seq[String],
      workExperience : Seq[String]) {

    def toProfile(loginInfo : LoginInfo) : Profile = {
      Profile(
          loginInfo = loginInfo,
          name = name,
          picture = Some(picture).filter(noValueFilter),
          title = Some(title).filter(noValueFilter),
          summary = Some(summary).filter(noValueFilter),
          location = Some(location).filter(noValueFilter),
          email = Some(email).filter(noValueFilter),
          website = Some(website).filter(noValueFilter),
          twitterUrl = Some(twitterUrl).filter(noValueFilter),
          gitHubUrl = Some(gitHubUrl).filter(noValueFilter),
          gitHubUsername = Some(gitHubUsername).filter(noValueFilter),
          projects = projects.map(convertProjectJson).toList,
          workExperience = workExperience.map(convertWorkExperienceJson).toList
      )
    }

    private def noValueFilter(item : String) : Boolean = (!item.isEmpty && item != NO_VALUE)
    private def convertProjectJson(project : String) : Project = (Json.parse(project).as[Project])
    private def convertWorkExperienceJson(workExperience : String) : WorkExperience = (Json.parse(workExperience).as[WorkExperience])
  }

  val form = Form(
    mapping(
      "Name" -> nonEmptyText,
      "Picture" -> text,
      "Title" -> text,
      "Summary" -> text,
      "Location" -> text,
      "Email" -> text,
      "Website" -> text,
      "TwitterUrl" -> text,
      "GitHubUrl" -> text,
      "GitHubUsername" -> text,
      "Projects" -> seq(text),
      "WorkExperience" -> seq(text)
    )(Data.apply)(Data.unapply)
  )

  object Data {
    def apply(profile : Profile) : Data = {
      Data(
          name = profile.name,
          picture = convertOption(profile.picture),
          title = convertOption(profile.title),
          summary = convertOption(profile.summary),
          location = convertOption(profile.location),
          email = convertOption(profile.email),
          website = convertOption(profile.website),
          twitterUrl = convertOption(profile.twitterUrl),
          gitHubUrl = convertOption(profile.gitHubUrl),
          gitHubUsername = convertOption(profile.gitHubUsername),
          projects = profile.projects.map(convertProject),
          workExperience = profile.workExperience.map(convertWorkExperience))
    }

    private def convertOption(opt : Option[String]) : String = opt.getOrElse(NO_VALUE)
    private def convertProject(project : Project) : String = Json.toJson(project).toString
    private def convertWorkExperience(workExperience : WorkExperience) : String = Json.toJson(workExperience).toString
  }
}
