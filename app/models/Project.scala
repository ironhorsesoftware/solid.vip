package models

import play.api.libs.json._

case class Project (title : String, description : String, link : String, thumbnail : Option[String], isContributedTo : Boolean)

object Project {
  implicit val ProjectWrites = Json.writes[Project]
  implicit val ProjectReads = Json.reads[Project]
}
