package daos.slick

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo

import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import daos.CredentialsDAO
import models.Credentials

class SlickCredentialsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends CredentialsDAO {
  val classTag = scala.reflect.classTag[PasswordInfo]
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbCredentials(tag : Tag) extends Table[Credentials](tag, "credentials") {
    def providerKey = column[String]("provider_key", O.PrimaryKey)
    def password = column[String]("password")
    def passwordHasher = column[String]("password_hasher")
    def passwordSalt = column[Option[String]]("password_salt")

    def * = (providerKey, password, passwordHasher, passwordSalt) <> (Credentials.tupled, Credentials.unapply)
  }

  private val credentials = TableQuery[DbCredentials]

  def add(loginInfo : LoginInfo, authInfo : PasswordInfo) : Future[PasswordInfo] = db.run {
    for {
      _ <- (credentials += Credentials(loginInfo, authInfo))
    } yield authInfo
  }
  
  def find(loginInfo : LoginInfo) : Future[Option[PasswordInfo]] = db.run {
    credentials.filter(cred => cred.providerKey === loginInfo.providerKey).result.headOption.map ( r => r.map { c =>
      c.passwordInfo
    })
  }

  def remove(loginInfo : LoginInfo) : Future[Unit] = db.run {
    val q = for { creds <- credentials if creds.providerKey === loginInfo.providerKey } yield creds
    q.delete.map(_ => Unit)
  }

  def save(loginInfo : LoginInfo, authInfo : PasswordInfo) : Future[PasswordInfo] = db.run {
    for {
      _ <- credentials.insertOrUpdate(Credentials(loginInfo, authInfo))
    } yield authInfo
  }

  def update(loginInfo : LoginInfo, authInfo : PasswordInfo) : Future[PasswordInfo] = db.run {
    for {
      _ <- credentials.update(Credentials(loginInfo, authInfo))
    } yield authInfo
  }
}
