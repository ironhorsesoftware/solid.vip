package services

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, OAuth2Settings, CommonSocialProfile, CommonSocialProfileBuilder }
import com.mohiva.play.silhouette.impl.providers.oauth2.BaseLinkedInProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.LinkedInProvider

class SolidLinkedInProvider(
    protected val httpLayer : HTTPLayer,
    protected val stateHandler : SocialStateHandler,
    val settings : OAuth2Settings) extends BaseLinkedInProvider with CommonSocialProfileBuilder  {

  override type Self = SolidLinkedInProvider

  override val profileParser = new SolidLinkedInProfileParser

  override def withSettings(f : (Settings) => Settings) = {
    new SolidLinkedInProvider(httpLayer, stateHandler, f(settings))
  }
}

object SolidLinkedInProvider {
  val SpecifiedProfileError = LinkedInProvider.SpecifiedProfileError
  val ID = LinkedInProvider.ID

  val API = "https://api.linkedin.com/v2/me"
}