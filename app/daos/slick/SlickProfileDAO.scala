package daos.slick

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo

import daos.ProfileDAO
import models.Profile

class SlickProfileDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends ProfileDAO {
  private val logger = Logger("SlickProfileDAO")

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbProfile(tag : Tag) extends Table[(Int, String, String, String)](tag, "profiles") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def profileJson = column[String]("profile_json")

    def * = (id, providerId, providerKey, profileJson)
  }

  private val profiles = TableQuery[DbProfile]

  def create(profile : Profile) : Future[Unit] = {
    Future.failed(new UnsupportedOperationException)
  }

  def retrieve(loginInfo : LoginInfo) : Future[Option[Profile]] = {
    Future.failed(new UnsupportedOperationException)
  }

  def update(profile : Profile) : Future[Unit] = {
    Future.failed(new UnsupportedOperationException)
  }

  def delete(loginInfo : LoginInfo) : Future[Unit] = {
    Future.failed(new UnsupportedOperationException)
  }
}