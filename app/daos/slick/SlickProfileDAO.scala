package daos.slick

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._

import slick.jdbc.JdbcProfile

import com.mohiva.play.silhouette.api.LoginInfo

import daos.ProfileDAO
import models.{Profile, Project, WorkExperience}

class SlickProfileDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec : ExecutionContext) extends ProfileDAO {
  private val logger = Logger("SlickProfileDAO")

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DbProfile(tag : Tag) extends Table[(Int, String, String, String)](tag, "profiles") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def profileJson = column[String]("profile_json")

    def * = (id, providerId, providerKey, profileJson)
  }

  private val profiles = TableQuery[DbProfile]

  def create(profile : Profile) : Future[Unit] = {
    val newProfile = (0, profile.loginInfo.providerID, profile.loginInfo.providerKey, SlickProfileDAO.writeProfile(profile).toString)
    db.run(profiles += newProfile).map(_ => Unit)
  }

  def retrieve(loginInfo : LoginInfo) : Future[Option[Profile]] = db.run {
    profiles.filter(profile => profile.providerId === loginInfo.providerID && profile.providerKey === loginInfo.providerKey).result.headOption.map(dbProfileOpt => dbProfileOpt.map { dbProfile =>
      val loginInfo = LoginInfo(dbProfile._2, dbProfile._3)
      SlickProfileDAO.readProfile(loginInfo, Json.parse(dbProfile._4))
    })
  }

  def update(profile : Profile) : Future[Unit] = db.run {
    for {
      rowsAffected <- profiles.filter(p => p.providerId === profile.loginInfo.providerID && p.providerKey === profile.loginInfo.providerKey).map { row =>
          row.profileJson
        }.update(SlickProfileDAO.writeProfile(profile).toString)
      result <- rowsAffected match {
        case 0 => DBIO.failed(new IllegalArgumentException("No entries to update."))
        case _ => DBIO.successful(Unit)
      }
    } yield result
  }

  def delete(loginInfo : LoginInfo) : Future[Unit] = db.run {
    val q = for { profile <- profiles if profile.providerId === loginInfo.providerID && profile.providerKey === loginInfo.providerKey } yield profile
    q.delete.map(_ => Unit)
  }
}

object SlickProfileDAO {
  def writeProfile(profile : Profile) = Json.obj(
      "name" -> profile.name,
      "picture" -> profile.picture,
      "title" -> profile.title,
      "summary" -> profile.summary,
      "location" -> profile.location,
      "email" -> profile.email,
      "website" -> profile.website,
      "twitterUrl" -> profile.twitterUrl,
      "gitHubUrl" -> profile.gitHubUrl,
      "gitHubUsername" -> profile.gitHubUsername,
      "projects" -> profile.projects,
      "workExperiences" -> profile.workExperience
    )

  def readProfile(loginInfo : LoginInfo, json : JsValue) : Profile = {
    val name = (json \ "name").as[String]
    val picture = (json \ "picture").asOpt[String]
    val title = (json \ "title").asOpt[String]
    val summary = (json \ "summary").asOpt[String]
    val location = (json \ "location").asOpt[String]
    val email = (json \ "email").asOpt[String]
    val website = (json \ "website").asOpt[String]
    val twitterUrl = (json \ "twitterUrl").asOpt[String]
    val gitHubUrl = (json \ "gitHubUrl").asOpt[String]
    val gitHubUsername = (json \ "gitHubUsername").asOpt[String]
    val projects = (json \ "projects").as[List[Project]]
    val workExperiences = (json \ "workExperiences").as[List[WorkExperience]]

    Profile(
      loginInfo,
      name,
      picture,
      title,
      summary,
      location,
      email,
      website,
      twitterUrl,
      gitHubUrl,
      gitHubUsername,
      projects,
      workExperiences)
  }
}