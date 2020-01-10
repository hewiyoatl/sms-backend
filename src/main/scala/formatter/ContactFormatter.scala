package formatter

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Contact(email: Option[String],
                   subject: Option[String],
                   message: Option[String])

object ContactFormatter {

  val ContactReader: Reads[Contact] = (
    (JsPath \ "email").readNullable[String] and
      (JsPath \ "subject").readNullable[String] and
      (JsPath \ "message").readNullable[String]
    ) (Contact.apply _)

  val ContactWriter: Writes[Contact] = (
    (JsPath \ "email").writeNullable[String] and
      (JsPath \ "subject").writeNullable[String] and
      (JsPath \ "message").writeNullable[String]
    ) (unlift(Contact.unapply))
}

