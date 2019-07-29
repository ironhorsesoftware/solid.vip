package daos

import java.util.UUID

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo

import models.Profile

trait ProfileDAO {
  def create(profile : Profile) : Future[UUID]

  def retrieve(loginInfo : LoginInfo) : Future[Option[Profile]]

  def save(profile : Profile) : Future[Profile]

  def delete(id : UUID) : Future[Unit]
}
