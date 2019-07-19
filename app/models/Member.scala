package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

case class Member(id : UUID, username : String, name : String, email : Option[String], activated : Boolean) extends Identity {
  def loginInfo = LoginInfo(CredentialsProvider.ID, username)
}

object Member extends Function5[UUID, String, String, Option[String], Boolean, Member] {
  def apply(id : UUID, loginInfo : LoginInfo, name : String, email : Option[String], activated : Boolean) : Member = {
     Member(id, loginInfo.providerKey, name, email, activated)
  }
}
