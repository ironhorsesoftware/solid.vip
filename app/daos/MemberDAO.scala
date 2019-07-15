package daos

import java.util.UUID
import scala.concurrent.Future
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.Member

trait MemberDAO extends IdentityService[Member] {

  def create(user : Member) : Future[UUID]

  def retrieve(loginInfo : LoginInfo) : Future[Option[Member]]

  def find(id : UUID) : Future[Option[Member]]

  def update(user : Member) : Future[Member]

  def delete(id : UUID) : Future[Unit]
}