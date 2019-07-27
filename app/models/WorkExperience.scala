package models

case class WorkExperience (
    title : String,
    company : String,
    startDate : String,
    endDate : Option[String],
    description : Option[String]
)