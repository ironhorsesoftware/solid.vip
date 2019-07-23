package daos.slick

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import daos.OAuth2DAO


class SlickOAuth2DAO  @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext)  extends OAuth2DAO {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  def add(loginInfo : LoginInfo, authInfo : OAuth2Info) : Future[OAuth2Info] = Future.failed(new UnsupportedOperationException())

  def find(loginInfo : LoginInfo) : Future[Option[OAuth2Info]] = Future.failed(new UnsupportedOperationException())

  def remove(loginInfo : LoginInfo) : Future[Unit] = Future.failed(new UnsupportedOperationException())

  def save(loginInfo : LoginInfo, authInfo : OAuth2Info) : Future[OAuth2Info] = Future.failed(new UnsupportedOperationException())

  def update(loginInfo : LoginInfo, authInfo : OAuth2Info) : Future[OAuth2Info] = Future.failed(new UnsupportedOperationException())
}