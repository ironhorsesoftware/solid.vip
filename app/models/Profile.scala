package models

case class Profile (
    name : String,
    picture : Option[String],
    title : Option[String],
    summary : Option[String],
    location : Option[String],
    email : Option[String],
    website : Option[String],
    twitterUrl : Option[String],
    gitHubUrl : Option[String],
    gitHubUsername : Option[String],
    projects : List[Project],
    workExperience : List[WorkExperience])