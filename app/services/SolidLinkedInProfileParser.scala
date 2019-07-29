package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{SocialProfileParser, CommonSocialProfile, OAuth2Info}

import play.api.Logging
import play.api.libs.json.{JsValue, JsArray, JsNumber}

import models.Profile

class SolidLinkedInProfileParser extends SocialProfileParser[JsValue, Profile, OAuth2Info] with Logging {
  override def parse(json : JsValue, authInfo: OAuth2Info) = Future.successful {
    logger.debug(s"JSON: ${json}")
    parseProfile(json, authInfo)
  }

  def parseCommonSocialProfile(json : JsValue, authInfo : OAuth2Info) : CommonSocialProfile = {
    val userID = (json \ "id").as[String]
    val firstName = (json \ "localizedFirstName").asOpt[String].filter(item => !item.isEmpty)
    val lastName = (json \ "localizedLastName").asOpt[String].filter(item => !item.isEmpty)
    val fullName = (json \ "vanityName").asOpt[String].filter(item => !item.isEmpty)
    val avatarURL = findAvatarUrl(json)
    val email = (json \ "emailAddress").asOpt[String].filter(item => !item.isEmpty)

    CommonSocialProfile(
      loginInfo = LoginInfo(SolidLinkedInProvider.ID, userID),
      firstName = firstName,
      lastName = lastName,
      fullName = fullName,
      avatarURL = avatarURL,
      email = email)    
  }

  def parseProfile(json : JsValue, authInfo : OAuth2Info) : Profile = {
    val userID = (json \ "id").as[String]
    val firstName = (json \ "localizedFirstName").asOpt[String].filter(item => !item.isEmpty)
    val lastName = (json \ "localizedLastName").asOpt[String].filter(item => !item.isEmpty)

    val avatarUrlOpt = findAvatarUrl(json) 

    Profile(
        loginInfo = LoginInfo(SolidLinkedInProvider.ID, userID),
        name = firstName.getOrElse("") + " " + lastName.getOrElse(""),
        picture = avatarUrlOpt,
        title = None,
        summary = None,
        location = None,
        email = None,
        website = None,
        twitterUrl = None,
        gitHubUrl = None,
        gitHubUsername = None,
        projects = List(),
        workExperience = List()
    )
  }

  def findAvatarUrl(json : JsValue) : Option[String] = {
    (json \ "profilePicture" \ "displayImage~" \ "elements").as[JsArray].value.find { element =>
      (element \ "data" \ "com.linkedin.digitalmedia.mediaartifact.StillImage" \ "displaySize" \ "width").as[JsNumber].value >= 180 
    }.flatMap{ element =>
      (element \ "identifiers").as[JsArray].value.find(identifier => (identifier \ "index").as[JsNumber].value == 0) 
    }.map { identifier =>
      identifier \ "identifier"
    }.map { result =>
      result.as[String]
    }
  }
}