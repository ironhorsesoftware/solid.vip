package services

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, OAuth2Settings, CommonSocialProfile, CommonSocialProfileBuilder }
import com.mohiva.play.silhouette.impl.providers.oauth2.{BaseGitHubProvider, GitHubProvider}


class SolidGitHubProvider (
    protected val httpLayer : HTTPLayer,
    protected val stateHandler : SocialStateHandler,
    val settings : OAuth2Settings) extends BaseGitHubProvider with CommonSocialProfileBuilder {

  override type Self = SolidGitHubProvider

  //override type Profile = CommonSocialProfile

  override val profileParser = new SolidGitHubProfileParser

  override def withSettings(f : (Settings) => Settings) = {
    new SolidGitHubProvider(httpLayer, stateHandler, f(settings))
  }
}

object SolidGitHubProvider {
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