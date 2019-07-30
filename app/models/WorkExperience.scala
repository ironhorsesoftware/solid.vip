package models

import play.api.libs.json._

case class WorkExperience (
    title : String,
    company : String,
    startDate : String,
    endDate : Option[String],
    description : Option[String]
)

object WorkExperience {
  implicit val workExperienceReads = Json.reads[WorkExperience]
  implicit val workExperienceWrites = Json.writes[WorkExperience]
}
