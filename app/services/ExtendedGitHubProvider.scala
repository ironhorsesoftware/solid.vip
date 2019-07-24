package services

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, OAuth2Settings, CommonSocialProfile, CommonSocialProfileBuilder }
import com.mohiva.play.silhouette.impl.providers.oauth2.{BaseGitHubProvider, GitHubProvider}


class ExtendedGitHubProvider (
    protected val httpLayer : HTTPLayer,
    protected val stateHandler : SocialStateHandler,
    val settings : OAuth2Settings) extends BaseGitHubProvider with CommonSocialProfileBuilder  {

  override type Self = ExtendedGitHubProvider

  override val profileParser = new ExtendedGitHubProfileParser

  override def withSettings(f : (Settings) => Settings) = {
    new ExtendedGitHubProvider(httpLayer, stateHandler, f(settings))
  }
}

object ExtendedGitHubProvider {
  /**
   * The error messages.
   */
  val SpecifiedProfileError = GitHubProvider.SpecifiedProfileError

  /**
   * The GitHub constants.
   */
  val ID = GitHubProvider.ID
  val API = GitHubProvider.API

}