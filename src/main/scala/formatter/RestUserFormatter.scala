package formatter

import models.RestUserOutbound
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class RestUserInbound(id: Option[Long],
                           sucursalId: Option[Long],
                           mobile: Option[String],
                           firstName: Option[String],
                           lastName: Option[String],
                           password: Option[String],
                           userName: Option[String],
                           createdTimestamp: Option[DateTime])

object RestUserFormatter {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  val jodaDateWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(d: DateTime): JsValue = JsString(d.toString())
  }

  val jodaDateReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, DateTimeFormat.forPattern(dateFormat))
    )
  )

  val RestUserReader: Reads[RestUserInbound] = (
    (JsPath \ "user_id").readNullable[Long] and
      (JsPath \ "sucursal_id").readNullable[Long] and
      (JsPath \ "mobile").readNullable[String] and
      (JsPath \ "first_name").readNullable[String] and
      (JsPath \ "last_name").readNullable[String] and
      (JsPath \ "password").readNullable[String] and
      (JsPath \ "user_name").readNullable[String] and
      (JsPath \ "created_timestamp").readNullable[DateTime](jodaDateReads)
    )(RestUserInbound.apply _)

  val RestUserWriter: Writes[RestUserOutbound] = (
    (JsPath \ "user_id").writeNullable[Long] and
      (JsPath \ "first_name").writeNullable[String] and
      (JsPath \ "last_name").writeNullable[String] and
      (JsPath \ "mobile").writeNullable[String] and
      (JsPath \ "user_name").writeNullable[String]
    )(unlift(RestUserOutbound.unapply))
}

