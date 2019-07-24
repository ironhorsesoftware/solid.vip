package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, CommonSocialProfile, CommonSocialProfileBuilder }
import com.mohiva.play.silhouette.impl.providers.{OAuth2Settings, OAuth2Info}
import com.mohiva.play.silhouette.impl.providers.oauth2.{BaseLinkedInProvider, LinkedInProvider}
import com.mohiva.play.silhouette.impl.providers.oauth2.LinkedInProvider.SpecifiedProfileError

class SolidLinkedInProvider(
    protected val httpLayer : HTTPLayer,
    protected val stateHandler : SocialStateHandler,
    val settings : OAuth2Settings) extends BaseLinkedInProvider with CommonSocialProfileBuilder  {

  override type Self = SolidLinkedInProvider

  override val profileParser = new SolidLinkedInProfileParser

  override protected def buildProfile(authInfo: OAuth2Info): Future[Profile] = {
    httpLayer.url(urls("api")).withHttpHeaders(("Authorization", s"Bearer ${authInfo.accessToken}")).get().flatMap { response =>
      val json = response.json
      (json \ "serviceErrorCode").asOpt[Int] match {
        case Some(error) =>
          val message = (json \ "message").asOpt[String]
          val requestId = (json \ "requestId").asOpt[String]
          val status = (json \ "status").asOpt[Int]
          val timestamp = (json \ "timestamp").asOpt[Long]

          Future.failed(new ProfileRetrievalException(SpecifiedProfileError.format(id, error, message, requestId, status, timestamp)))
        case _ => profileParser.parse(json, authInfo)
      }
    }
  }

  override def withSettings(f : (Settings) => Settings) = {
    new SolidLinkedInProvider(httpLayer, stateHandler, f(settings))
  }
}

object SolidLinkedInProvider {
  val ID = LinkedInProvider.ID

  val API = "https://api.linkedin.com/v2/me"
}