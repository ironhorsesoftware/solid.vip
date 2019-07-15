package forms

import play.api.data.Form
import play.api.data.Forms._

object ContactForm {
	case class Data(name : String, email : String, subject : String, content : String)

	val form = Form(
		mapping(
			"name" -> nonEmptyText,
			"email" -> email,
			"subject" -> nonEmptyText,
			"content" -> nonEmptyText
		)(Data.apply)(Data.unapply)
	)
}
