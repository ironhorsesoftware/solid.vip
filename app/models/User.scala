package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

case class User(id : UUID, username : String, name : String) extends Identity

object User extends Function3[UUID, String, String, User] {
  def apply(id : UUID, loginInfo : LoginInfo, name : String) : User = {
     User(id, loginInfo.providerKey, name)
  }
}
