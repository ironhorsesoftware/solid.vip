package services

import scala.concurrent.Future
import scala.util.{Try, Success, Failure}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.{SocialProfileParser, CommonSocialProfile, OAuth2Info}
import com.mohiva.play.silhouette.impl.providers.oauth2.LinkedInProvider.ID

import play.api.Logging
import play.api.libs.json.{JsValue, JsArray, JsNumber, JsString, JsBoolean}

import models.{Profile, WorkExperience}

class SolidGoogleProfileParser extends SocialProfileParser[JsValue, Profile, OAuth2Info] with Logging {
  override def parse(json : JsValue, authInfo : OAuth2Info) : Future[Profile] = {
    logger.debug(s"JSON: ${json}")

    val profileTry =
      getId(json).flatMap { id =>
        getName(json) map { name =>
          Profile(
              loginInfo = LoginInfo(SolidGoogleProvider.ID, id),
              name = name,
              picture = getPrimaryItem(json, "photos", "url"),
              title = getPrimaryItem(json, "organizations", "title"),
              summary = None,
              location = None,
              email = getPrimaryItem(json, "emailAddresses", "value"),
              website = None,
              twitterUrl = None,
              gitHubUrl = None,
              gitHubUsername = None,
              projects = List(),
              workExperience = getWorkExperience(json)
          )
        }
      }

    Future.fromTry(profileTry)
  }

  def getId(json : JsValue) : Try[String] = {
    (json \\ "id").headOption.map { id =>
      id.as[JsString].value.toString
    } match {
      case Some(id) => Success(id)
      case _ => Failure(new ProviderException("The Google profile did not have an ID."))
    }
  }

  def isPrimary(item : JsValue) : Boolean = {
    (item \ "metadata" \ "primary").as[JsBoolean].value
  }

  def getPrimaryItem(json : JsValue, category : String, record: String) : Option[String] = {
    (json \ category).as[JsArray].value.find(isPrimary).map { item =>
      (item \ record).as[JsString].value.toString
    }.filter(item => !item.isEmpty)
  }

  def getName(json : JsValue) : Try[String] = {
    val displayNameOpt = getPrimaryItem(json, "names", "displayName")

    displayNameOpt match {
      case Some(displayName) => Success(displayName)
      case _ => Failure(new ProviderException("The Google profile did not have a primary name."))
    }
  }

  def getWorkExperience(json : JsValue) : List[WorkExperience] = {
    (json \ "organizations").as[JsArray].value.filter { organization =>
      (organization \ "type").as[JsString].value == "work"
    }.map { workExperience =>
      WorkExperience(
          title = (workExperience \ "title").as[JsString].value.toString,
          company = (workExperience \ "name").as[JsString].value.toString,
          startDate = (workExperience \ "startDate" \ "year").as[JsNumber].value.toString,
          endDate = (workExperience \ "endDate" \ "year").asOpt[JsNumber].map(year => year.toString),
          description = None,
      )
    }.toList
  }
}
