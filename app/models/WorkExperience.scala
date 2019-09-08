package models

import play.api.libs.json._

case class WorkExperience (
    title : String,
    company : String,
    startDate : String,
    endDate : Option[String],
    description : Option[String]
) extends Ordered[WorkExperience] {

  def compare (that : WorkExperience) = {

    var comparison = compareEndDates(that)

    if (comparison == 0) {
      comparison = startDate.compare(that.startDate)
    }

    if (comparison == 0) {
      comparison = company.compare(that.company)
    }

    if (comparison == 0) {
      comparison = title.compare(that.title)
    }

    comparison
  }

  def compareEndDates(that : WorkExperience) = {
    val thisEndDateIsDefined = endDate.isDefined
    val thatEndDateIsDefined = that.endDate.isDefined

    if (thisEndDateIsDefined && !thatEndDateIsDefined) {
      1

    } else if (!thisEndDateIsDefined && thatEndDateIsDefined) {
      -1

    } else if (!thisEndDateIsDefined && !thatEndDateIsDefined) {
      0

    } else {
      // End dates should be sorted in reverse order.
      that.endDate.get.compare(this.endDate.get)
    }
  }
}

object WorkExperience {
  implicit val workExperienceReads = Json.reads[WorkExperience]
  implicit val workExperienceWrites = Json.writes[WorkExperience]
}
