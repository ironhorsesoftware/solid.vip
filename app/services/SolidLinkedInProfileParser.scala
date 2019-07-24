package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{SocialProfileParser, CommonSocialProfile, OAuth2Info}
import com.mohiva.play.silhouette.impl.providers.oauth2.LinkedInProvider.ID

import play.api.Logging
import play.api.libs.json.JsValue

class SolidLinkedInProfileParser extends SocialProfileParser[JsValue, CommonSocialProfile, OAuth2Info] with Logging {
  override def parse(json : JsValue, authInfo: OAuth2Info) = Future.successful {
    logger.info(s"JSON: ${json} | authInfo: ${authInfo}")

    val userID = (json \ "id").as[String]
    val firstName = (json \ "localizedFirstName").asOpt[String]
    val lastName = (json \ "localizedLastName").asOpt[String]
    val fullName = (json \ "vanityName").asOpt[String]
    val avatarURL = (json \ "profilePicture" \ "displayImage").asOpt[String]
    val email = (json \ "emailAddress").asOpt[String]

    CommonSocialProfile(
      loginInfo = LoginInfo(ID, userID),
      firstName = firstName,
      lastName = lastName,
      fullName = fullName,
      avatarURL = avatarURL,
      email = email)
  }
}