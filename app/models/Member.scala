package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

case class Member(id : UUID, username : String, name : String) extends Identity

object Member extends Function3[UUID, String, String, Member] {
  def apply(id : UUID, loginInfo : LoginInfo, name : String) : Member = {
     Member(id, loginInfo.providerKey, name)
  }
}
