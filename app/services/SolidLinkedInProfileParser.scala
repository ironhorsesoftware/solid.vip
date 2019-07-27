package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{SocialProfileParser, CommonSocialProfile, OAuth2Info}
import com.mohiva.play.silhouette.impl.providers.oauth2.LinkedInProvider.ID

import play.api.Logging
import play.api.libs.json.{JsValue, JsArray, JsNumber}

import org.apache.jena.rdf.model.{Model, ModelFactory}

class SolidLinkedInProfileParser extends SocialProfileParser[JsValue, CommonSocialProfile, OAuth2Info] with Logging {
  override def parse(json : JsValue, authInfo: OAuth2Info) = Future.successful {
    logger.info(s"JSON: ${json}")
    parseCommonSocialProfile(json, authInfo)
  }

  def parseCommonSocialProfile(json : JsValue, authInfo : OAuth2Info) : CommonSocialProfile = {
    val userID = (json \ "id").as[String]
    val firstName = (json \ "localizedFirstName").asOpt[String]
    val lastName = (json \ "localizedLastName").asOpt[String]
    val fullName = (json \ "vanityName").asOpt[String]
    val avatarURL = findAvatarUrl(json)
    val email = (json \ "emailAddress").asOpt[String]

    CommonSocialProfile(
      loginInfo = LoginInfo(ID, userID),
      firstName = firstName,
      lastName = lastName,
      fullName = fullName,
      avatarURL = avatarURL,
      email = email)    
  }

  def parseRdfModel(json : JsValue, authInfo : OAuth2Info) : Model = {
    val model = ModelFactory.createDefaultModel()

    val userID = (json \ "id").as[String]
    val firstName = (json \ "localizedFirstName").asOpt[String]
    val lastName = (json \ "localizedLastName").asOpt[String]

    val avatarUrlOpt = findAvatarUrl(json) 

    model
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