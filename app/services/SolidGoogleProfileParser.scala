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
    logger.info(s"JSON: ${json}")

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
    (item \ "metadata" \ "primary").asInstanceOf[JsBoolean].value
  }

  def getPrimaryItem(json : JsValue, category : String, record: String) : Option[String] = {
    (json \ category).as[JsArray].value.find(isPrimary).map { item =>
      (item \ record).as[JsString].value.toString
    }
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
      (organization \ "type").asInstanceOf[JsString].value == "work"
    }.map { workExperience =>
      val endDateOpt =
        if ((workExperience \ "current").asInstanceOf[JsBoolean].value) {
          None
        } else {
          Some((workExperience \ "endDate" \ "year").asInstanceOf[JsNumber].value.toString)
        }

      WorkExperience(
          title = (workExperience \ "title").asInstanceOf[JsString].value.toString,
          company = (workExperience \ "name").asInstanceOf[JsString].value.toString,
          startDate = (workExperience \ "startDate" \ "year").asInstanceOf[JsNumber].value.toString,
          endDate = endDateOpt,
          description = None,
      )
    }.toList
  }
}
