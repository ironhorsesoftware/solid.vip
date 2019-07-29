package daos.mongodb

import javax.inject.Inject
import java.util.UUID

import scala.concurrent.{Future, ExecutionContext}

import com.mohiva.play.silhouette.api.LoginInfo

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi

import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import models.{Profile, Project, WorkExperience}
import daos.ProfileDAO

class MongoProfileDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends ProfileDAO {

  implicit lazy val loginInfoFormat = Json.format[LoginInfo]
  implicit lazy val workExperienceFormat = Json.format[WorkExperience]
  implicit lazy val projectFormat = Json.format[Project]
  implicit lazy val profileFormat = Json.format[Profile]

  def profiles = reactiveMongoApi.database.map(_.collection[JSONCollection]("profiles"))

  def create(profile : Profile) : Future[Unit] = {
    profiles.flatMap(_.insert.one(profile)).map(_ => Unit)
  }

  def retrieve(loginInfo : LoginInfo) : Future[Option[Profile]] = {
    val projection : Option[Profile] = None

    profiles.flatMap { collection =>
      collection.find(Json.obj("loginInfo" -> loginInfo), projection).one[Profile]
    }
  }

  def update(profile : Profile) : Future[Unit] = {
    profiles.flatMap(_.update(ordered = false).one(Json.obj("loginInfo" -> profile.loginInfo), profile)).map {
      _ => Unit
    }
  }

  def delete(loginInfo : LoginInfo) : Future[Unit] = {
    profiles.flatMap(_.delete.one(Json.obj("loginInfo" -> loginInfo))).map(_ => Unit)
  }
}
