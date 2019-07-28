package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, CommonSocialProfile, CommonSocialProfileBuilder}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Settings, OAuth2Info}
import com.mohiva.play.silhouette.impl.providers.oauth2.{BaseGoogleProvider, GoogleProvider}
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider.SpecifiedProfileError

class SolidGoogleProvider(
    protected val httpLayer : HTTPLayer,
    protected val stateHandler : SocialStateHandler,
    val settings : OAuth2Settings) extends BaseGoogleProvider with SolidProfileBuilder {

  override type Self = SolidGoogleProvider

  override val profileParser = new SolidGoogleProfileParser

  override def withSettings(f : (Settings) => Settings) = {
    new SolidGoogleProvider(httpLayer, stateHandler, f(settings))
  }
}

object SolidGoogleProvider {
  val ID = GoogleProvider.ID
  val SpecifiedProfileError = GoogleProvider.SpecifiedProfileError

  val API = "https://people.googleapis.com/v1/people/me?personFields=names,photos,emailAddresses,locales,organizations,skills,urls,taglines&access_token=%s"
}