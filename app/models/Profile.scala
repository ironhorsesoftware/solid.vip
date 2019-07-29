package models

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.SocialProfile

case class Profile (
    loginInfo : LoginInfo,
    name : String,
    picture : Option[String],
    title : Option[String],
    summary : Option[String],
    location : Option[String],
    email : Option[String],
    website : Option[String],
    twitterUrl : Option[String],
    gitHubUrl : Option[String],
    gitHubUsername : Option[String],
    projects : List[Project],
    workExperience : List[WorkExperience]) extends SocialProfile

object Profile {
  def apply(member : Member) : Profile = 
    Profile(
        loginInfo = member.loginInfo,
        name = member.name,
        picture = None,
        title = None,
        summary = None,
        location = None,
        email = member.email,
        website = None,
        twitterUrl = None,
        gitHubUrl = None,
        gitHubUsername = None,
        projects = List(),
        workExperience = List()
    )
}