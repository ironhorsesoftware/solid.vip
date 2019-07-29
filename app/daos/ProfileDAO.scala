package daos

import java.util.UUID

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo

import models.Profile

trait ProfileDAO {
  def create(profile : Profile) : Future[Unit]

  def retrieve(loginInfo : LoginInfo) : Future[Option[Profile]]

  def update(profile : Profile) : Future[Unit]

  def delete(loginInfo : LoginInfo) : Future[Unit]
}
