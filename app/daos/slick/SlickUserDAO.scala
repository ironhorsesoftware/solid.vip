package daos.slick

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo

import daos.UserDAO
import models.{User, UserRole}

class SlickUserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends UserDAO {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private implicit val roleColumnType = MappedColumnType.base[UserRole.UserRole, String](
      { r => r.toString },
      { s => UserRole.values.find(_.toString() == s).get }
  )

  private class Users(tag : Tag) extends Table[User](tag, "client_user") {
    def id = column[UUID]("id", O.PrimaryKey)
    def clientId = column[UUID]("client_id")
    def role = column[UserRole.UserRole]("role")
    def email = column[String]("email")
    def password = column[String]("password")

    def * = (id, clientId, role, email, password) <> (User.tupled, User.unapply)
  }

  private val users = TableQuery[Users]

  def create(user : User) : Future[UUID] = db.run {
      (users returning users.map(_.id)) += user
  }

  def retrieve(loginInfo : LoginInfo) : Future[Option[User]] = db.run {
    users.filter(user => user.email === loginInfo.providerID && user.password === loginInfo.providerKey).result.headOption
  }

  def find(id : UUID) : Future[Option[User]] = db.run {
    users.filter(user => user.id === id).result.headOption
  }

  def update(user : User) : Future[User] = db.run {
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