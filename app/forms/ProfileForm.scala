package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._

import models.{Profile, Project, WorkExperience}

object ProfileForm {
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
      workExperience : Seq[String]
  )

  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "picture" -> text,
      "title" -> text,
      "summary" -> text,
      "location" -> text,
      "email" -> text,
      "website" -> text,
      "twitterUrl" -> text,
      "gitHubUrl" -> text,
      "gitHubUsername" -> text,
      "projects" -> seq(text),
      "workExperience" -> seq(text)
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

    private def convertOption(opt : Option[String]) : String = {
      opt.getOrElse("None")
    }

    private def convertProject(project : Project) : String = {
      Json.toJson(project).toString
    }

    private def convertWorkExperience(workExperience : WorkExperience) : String = {
      Json.toJson(workExperience).toString
    }
  }
}
