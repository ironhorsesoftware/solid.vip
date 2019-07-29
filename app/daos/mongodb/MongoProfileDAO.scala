package daos.mongodb

import javax.inject.Inject
import java.util.UUID

import scala.concurrent.{Future, ExecutionContext}

import com.mohiva.play.silhouette.api.LoginInfo

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import models.Profile
import daos.ProfileDAO

class MongoProfileDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends ProfileDAO {

  def create(profile : Profile) = Future.failed(new UnsupportedOperationException())

  def retrieve(loginInfo : LoginInfo) = Future.failed(new UnsupportedOperationException())

  def save(profile : Profile) = Future.failed(new UnsupportedOperationException())

  def delete(id : UUID) = Future.failed(new UnsupportedOperationException())
}
