package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

object UserRole extends Enumeration {
  type UserRole = Value
  val Admin = Value("ADMIN")
  val Developer = Value("DEVELOPER")
}

case class User(id : UUID, clientId : UUID, role : UserRole.UserRole, email : String, password : String) extends Identity {
  def loginInfo : LoginInfo = {
    LoginInfo(email, password)
  }
}

object User extends Function5[UUID, UUID, UserRole.UserRole, String, String, User] {
  def apply(id : UUID, clientId : UUID, role : UserRole.UserRole, loginInfo : LoginInfo) : User = {
     User(id, clientId, role, loginInfo.providerID, loginInfo.providerKey)
  }
}
