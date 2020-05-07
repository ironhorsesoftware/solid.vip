package services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import models.Member
import daos.MemberDAO

import play.api.Logging

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 * @param ex      The execution context.
 */
class MemberServiceImpl @Inject() (memberDAO: MemberDAO, credentialsDao : DelegableAuthInfoDAO[PasswordInfo])(implicit ex: ExecutionContext) extends MemberService with Logging {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID) = memberDAO.find(id)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[Member]] = memberDAO.retrieve(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(member: Member) = {
    memberDAO.find(member.id).flatMap {
      case Some(foundMember) => {
        memberDAO.update(member)
      }
      case None => {
        for {
          _ <- memberDAO.create(member)
        } yield {
          member
        }
      }
    }
  }


  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    memberDAO.retrieve(profile.loginInfo).flatMap {
      case Some(member) => // Update user with profile
        memberDAO.update(member.copy(
          name = profile.fullName.getOrElse(profile.loginInfo.providerKey),
          email = profile.email
        ))
      case None => // Insert a new user
        val member = Member(
          id = UUID.randomUUID(),
          username = profile.loginInfo.providerKey,
          name = profile.fullName.getOrElse(profile.loginInfo.providerKey),
          email = profile.email,
          activated = true
        )
        for {
          _ <- memberDAO.create(member)
        } yield {
          member
        }
    }
  }

  def delete(member: Member) = {
    credentialsDao.remove(member.loginInfo).flatMap { r =>
      memberDAO.delete(member.id)
    }
  }
}
