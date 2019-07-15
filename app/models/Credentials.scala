package models

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

case class Credentials (providerKey : String, password : String, passwordHasher : String, passwordSalt : Option[String]) {
  def loginInfo = LoginInfo(CredentialsProvider.ID, providerKey)
  def passwordInfo = PasswordInfo(passwordHasher, password, passwordSalt)
}

object Credentials extends Function4[String, String, String, Option[String], Credentials] {
  def apply(loginInfo : LoginInfo, passwordInfo : PasswordInfo) : Credentials = {
    Credentials(loginInfo.providerKey, passwordInfo.password, passwordInfo.hasher, passwordInfo.salt)
  }
}
