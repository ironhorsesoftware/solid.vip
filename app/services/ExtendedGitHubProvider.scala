package services

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, OAuth2Settings, CommonSocialProfile}
import com.mohiva.play.silhouette.impl.providers.oauth2.BaseGitHubProvider


class ExtendedGitHubProvider (
    protected val httpLayer : HTTPLayer,
    protected val stateHandler : SocialStateHandler,
    val settings : OAuth2Settings) extends BaseGitHubProvider {

  type Self = ExtendedGitHubProvider

  type Profile = CommonSocialProfile

  val profileParser = new ExtendedGitHubProfileParser

  def withSettings(f : (Settings) => Settings) = {
    new ExtendedGitHubProvider(httpLayer, stateHandler, f(settings))
  }
}