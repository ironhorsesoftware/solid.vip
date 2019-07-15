package forms

import play.api.data.Form
import play.api.data.Forms._

object ContactForm {
	case class Data(name : String, company : String, email : String, subject : String, content : String)

	val form = Form(
		mapping(
			"name" -> nonEmptyText,
			"company" -> text,
			"email" -> email,
			"subject" -> nonEmptyText,
			"content" -> nonEmptyText
		)(Data.apply)(Data.unapply)
	)
}
