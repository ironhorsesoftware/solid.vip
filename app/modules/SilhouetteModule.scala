package modules

import javax.inject.Inject

import scala.concurrent.ExecutionContext

import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.{ Env, Environment, EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.api.crypto.{Signer, Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.services.{AuthenticatorService}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{CacheLayer, PasswordHasherRegistry, FingerprintGenerator, IDGenerator, Clock, PasswordInfo}
import com.mohiva.play.silhouette.api.util.{HTTPLayer, PlayHTTPLayer}
import com.mohiva.play.silhouette.crypto.{JcaSigner, JcaSignerSettings, JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorSettings, CookieAuthenticatorService}
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorSettings, JWTAuthenticatorService}
import com.mohiva.play.silhouette.impl.providers.{ OAuth1TokenSecretProvider, OAuth1Settings, OAuth1Info, OAuth2Settings, OAuth2Info, OpenIDInfo}
import com.mohiva.play.silhouette.impl.providers.{SocialStateHandler, DefaultSocialStateHandler, SocialProviderRegistry }
import com.mohiva.play.silhouette.impl.providers.oauth1.TwitterProvider
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{CookieSecretProvider, CookieSecretSettings}
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2.{GitHubProvider, LinkedInProvider}
import com.mohiva.play.silhouette.impl.providers.state.{ CsrfStateItemHandler, CsrfStateSettings }
import com.mohiva.play.silhouette.impl.util.{PlayCacheLayer, DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.typesafe.config.Config

import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule

import play.api.Configuration
import play.api.mvc.{Cookie, CookieHeaderEncoding}
import play.api.libs.ws.WSClient

import daos.{MemberDAO, CredentialsDAO, AuthTokenDAO, AuthTokenDAOImpl}
import daos.slick.{SlickMemberDAO, SlickCredentialsDAO}
import models.Member
import services.{AuthTokenService, AuthTokenServiceImpl, MemberService, MemberServiceImpl}

import scala.concurrent.ExecutionContext.Implicits.global

trait InteractiveEnv extends Env {
  type I = Member
  type A = CookieAuthenticator
}

trait ApiEnv extends Env {
  type I = Member
  type A = JWTAuthenticator
}

class SilhouetteModule @Inject() extends AbstractModule with ScalaModule  {

  /**
   * A very nested optional reader, to support these cases:
   * Not set, set None, will use default ('Lax')
   * Set to null, set Some(None), will use 'No Restriction'
   * Set to a string value try to match, Some(Option(string))
   */
  implicit val sameSiteReader: ValueReader[Option[Option[Cookie.SameSite]]] =
    (config: Config, path: String) => {
      if (config.hasPathOrNull(path)) {
        if (config.getIsNull(path))
          Some(None)
        else {
          Some(Cookie.SameSite.parse(config.getString(path)))
        }
      } else {
        None
      }
    }

  override def configure() {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]

    bind[MemberDAO].to[SlickMemberDAO]
    bind[MemberService].to[MemberServiceImpl]

    bind[CredentialsDAO].to[SlickCredentialsDAO]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[CredentialsDAO]

    bind[DelegableAuthInfoDAO[OAuth1Info]].toInstance(new InMemoryAuthInfoDAO[OAuth1Info])
    bind[DelegableAuthInfoDAO[OAuth2Info]].toInstance(new InMemoryAuthInfoDAO[OAuth2Info])
    bind[DelegableAuthInfoDAO[OpenIDInfo]].toInstance(new InMemoryAuthInfoDAO[OpenIDInfo])

    bind[Silhouette[InteractiveEnv]].to[SilhouetteProvider[InteractiveEnv]]

    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  /**
   * Provides the HTTP layer implementation.
   *
   * @param client Play's WS client.
   * @return The HTTP layer implementation.
   */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  @Provides
  def provideInteractiveEnvironment(
      userService: MemberDAO,
      authenticatorService: AuthenticatorService[CookieAuthenticator],
      eventBus: EventBus) : Environment[InteractiveEnv] = {
    Environment[InteractiveEnv](userService, authenticatorService, Seq(), eventBus)
  }

  /* TODO: In the API Environment, the "Client" is the user.
  @Provides
  def provideApiEnvironment(
      userService : UserDAO,
      authenticatorService: AuthenticatorService[JWTAuthenticator],
      eventBus : EventBus) : Environment[ApiEnv] = {
    Environment[ApiEnv](userService, authenticatorService, Seq(), eventBus)
  }
  */

  /**
   * Provides the authenticator service.
   *
   * @param fingerprintGenerator The fingerprint generator implementation.
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The authenticator service.
   */
  @Provides
  def provideCookieAuthenticatorService(
    @Named("authenticator-signer") signer: Signer,
    @Named("authenticator-crypter") crypter: Crypter,
    cookieHeaderEncoding: CookieHeaderEncoding,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock): AuthenticatorService[CookieAuthenticator] = {

    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(config, None, signer, cookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, idGenerator, clock)
  }

  @Provides @Named("authenticator-signer")
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")

    new JcaSigner(config)
  }

  @Provides @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  @Provides
  def provideAuthInfoRepository(passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo]) : AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDAO)
  }

  /**
   * Provides the social state handler.
   *
   * @param signer The signer implementation.
   * @return The social state handler implementation.
   */
  @Provides
  def provideSocialStateHandler(
    @Named("social-state-signer") signer: Signer,
    csrfStateItemHandler: CsrfStateItemHandler): SocialStateHandler = {

    new DefaultSocialStateHandler(Set(csrfStateItemHandler), signer)
  }

  @Provides
  def provideCsrfStateItemHandler(
    idGenerator: IDGenerator,
    @Named("csrf-state-item-signer") signer: Signer,
    configuration: Configuration): CsrfStateItemHandler = {
    val settings = configuration.underlying.as[CsrfStateSettings]("silhouette.csrfStateItemHandler")
    new CsrfStateItemHandler(settings, idGenerator, signer)
  }

  @Provides @Named("csrf-state-item-signer")
  def provideCSRFStateItemSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.csrfStateItemHandler.signer")

    new JcaSigner(config)
  }

  @Provides @Named("social-state-signer")
  def provideSocialStateSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.socialStateHandler.signer")

    new JcaSigner(config)
  }


  @Provides
  def provideLinkedInProvider(httpLayer: HTTPLayer, socialStateHandler: SocialStateHandler, configuration: Configuration): LinkedInProvider = {
    new LinkedInProvider(httpLayer, socialStateHandler, configuration.underlying.as[OAuth2Settings]("silhouette.linkedin"))
  }

  @Provides
  def provideGitHubProvider(httpLayer: HTTPLayer, socialStateHandler: SocialStateHandler, configuration: Configuration): GitHubProvider = {
    new GitHubProvider(httpLayer, socialStateHandler, configuration.underlying.as[OAuth2Settings]("silhouette.github"))
  }

  /**
   * Provides the Twitter provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param tokenSecretProvider The token secret provider implementation.
   * @param configuration The Play configuration.
   * @return The Twitter provider.
   */
  @Provides
  def provideTwitterProvider(
    httpLayer: HTTPLayer,
    tokenSecretProvider: OAuth1TokenSecretProvider,
    configuration: Configuration): TwitterProvider = {

    val settings = configuration.underlying.as[OAuth1Settings]("silhouette.twitter")
    new TwitterProvider(httpLayer, new PlayOAuth1Service(settings), tokenSecretProvider, settings)
  }

  /**
   * Provides the OAuth1 token secret provider.
   *
   * @param signer The signer implementation.
   * @param crypter The crypter implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The OAuth1 token secret provider implementation.
   */
  @Provides
  def provideOAuth1TokenSecretProvider(
    @Named("oauth1-token-secret-signer") signer: Signer,
    @Named("oauth1-token-secret-crypter") crypter: Crypter,
    configuration: Configuration,
    clock: Clock): OAuth1TokenSecretProvider = {

    val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
    new CookieSecretProvider(settings, signer, crypter, clock)
  }

  @Provides @Named("oauth1-token-secret-signer")
  def provideOAuth1TokenSecretSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.oauth1TokenSecretProvider.signer")

    new JcaSigner(config)
  }

  @Provides @Named("oauth1-token-secret-crypter")
  def provideOAuth1TokenSecretCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")

    new JcaCrypter(config)
  }

  @Provides
  def provideSocialProviderRegistry(
    linkedInProvider : LinkedInProvider,
    gitHubProvider : GitHubProvider,
    twitterProvider: TwitterProvider): SocialProviderRegistry = {

    SocialProviderRegistry(Seq(
      linkedInProvider,
      gitHubProvider,
      twitterProvider
    ))
  }

}
