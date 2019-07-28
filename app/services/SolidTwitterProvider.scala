package services

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, CommonSocialProfile, CommonSocialProfileBuilder}
import com.mohiva.play.silhouette.impl.providers.{OAuth1Settings, OAuth1Info, OAuth1Service, OAuth1TokenSecretProvider}
import com.mohiva.play.silhouette.impl.providers.oauth1.{BaseTwitterProvider, TwitterProvider}
import com.mohiva.play.silhouette.impl.providers.oauth1.TwitterProvider.SpecifiedProfileError

import models.Profile

class SolidTwitterProvider(
    protected val httpLayer : HTTPLayer,
    val service: OAuth1Service,
    protected val stateHandler : SocialStateHandler,
    protected val tokenSecretProvider: OAuth1TokenSecretProvider,
    val settings : OAuth1Settings) extends BaseTwitterProvider with SolidProfileBuilder {

  override type Self = SolidTwitterProvider
  override val profileParser = new SolidTwitterProfileParser

  override def withSettings(f : (Settings) => Settings) = {
    new SolidTwitterProvider(httpLayer, service.withSettings(f), stateHandler, tokenSecretProvider, f(settings))
  }
}