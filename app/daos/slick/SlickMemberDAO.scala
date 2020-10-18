package daos.slick

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider

import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo

import daos.MemberDAO
import models.Member

class SlickMemberDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends MemberDAO {
  private val logger = Logger("SlickMemberDAO")

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class Members(tag : Tag) extends Table[Member](tag, "members") {
    def id = column[UUID]("id", O.PrimaryKey)
    def username = column[String]("username")
    def name = column[String]("name")
    def email = column[Option[String]]("email")
    def activated = column[Boolean]("is_activated")

    def * = (id, username, name, email, activated) <> (Member.tupled, Member.unapply)
  }

  private val members = TableQuery[Members]

  def all() : Future[Seq[Member]] = db.run {
    members.sortBy(_.username).result
  }

  def create(member : Member) : Future[UUID] = db.run {
      (members returning members.map(_.id)) += member
  }

  def retrieve(loginInfo : LoginInfo) : Future[Option[Member]] = db.run {
    members.filter(user => user.username === loginInfo.providerKey).result.headOption
  }

  def find(id : UUID) : Future[Option[Member]] = db.run {
    members.filter(member => member.id === id).result.headOption
  }

  def update(member : Member) : Future[Member] = {
    db.run {
      for {
        _ <- members.filter(mbr => mbr.id === member.id).update(member)
       updatedMember <- members.filter(mbr => mbr.id === member.id).result.head 
      } yield updatedMember
    }
  }

  def delete(id : UUID) : Future[Unit] = db.run {
    val q = for { member <- members if member.id === id } yield member
    q.delete.map(_ => Unit)
  }
}