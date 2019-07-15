package daos.slick

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo

import daos.MemberDAO
import models.Member

class SlickMemberDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends MemberDAO {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class Users(tag : Tag) extends Table[Member](tag, "client_user") {
    def id = column[UUID]("id", O.PrimaryKey)
    def username = column[String]("username")
    def name = column[String]("name")

    def * = (id, username, name) <> (Member.tupled, Member.unapply)
  }

  private val users = TableQuery[Users]

  def create(user : Member) : Future[UUID] = db.run {
      (users returning users.map(_.id)) += user
  }

  def retrieve(loginInfo : LoginInfo) : Future[Option[Member]] = db.run {
    users.filter(user => user.username === loginInfo.providerKey).result.headOption
  }

  def find(id : UUID) : Future[Option[Member]] = db.run {
    users.filter(user => user.id === id).result.headOption
  }

  def update(user : Member) : Future[Member] = db.run {
    for {
      _ <- users.update(user)
      usr <- users.filter(usr => usr.id === user.id).result.head
    } yield usr
  }

  def delete(id : UUID) : Future[Unit] = db.run {
    val q = for { user <- users if user.id === id } yield user
    q.delete.map(_ => Unit)
  }
}