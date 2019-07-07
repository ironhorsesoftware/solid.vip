package daos

import java.util.UUID
import scala.concurrent.Future
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.User

trait UserDAO extends IdentityService[User] {

  def create(user : User) : Future[UUID]

  def retrieve(loginInfo : LoginInfo) : Future[Option[User]]

  def find(id : UUID) : Future[Option[User]]

  def update(user : User) : Future[User]

  def delete(id : UUID) : Future[Unit]
}