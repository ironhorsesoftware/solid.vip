package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, CommonSocialProfile, CommonSocialProfileBuilder}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Settings, OAuth2Info}
import com.mohiva.play.silhouette.impl.providers.oauth2.{BaseFacebookProvider, FacebookProvider}
import com.mohiva.play.silhouette.impl.providers.oauth2.LinkedInProvider.SpecifiedProfileError

class SolidFacebookProvider(
    protected val httpLayer : HTTPLayer,
    protected val stateHandler : SocialStateHandler,
    val settings : OAuth2Settings) extends BaseFacebookProvider with SolidProfileBuilder {

  override type Self = SolidFacebookProvider

  override val profileParser = new SolidFacebookParser

  override def withSettings(f : (Settings) => Settings) = {
    new SolidFacebookProvider(httpLayer, stateHandler, f(settings))
  }
}

object SolidFacebookProvider {
  val ID = FacebookProvider.ID
  val SpecifiedProfileError = FacebookProvider.SpecifiedProfileError
}
