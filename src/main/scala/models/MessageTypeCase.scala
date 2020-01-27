package models

import com.google.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MessageTypeCase(id: Option[Long],
                           name: Option[String],
                           description: Option[String])

//case class MessageTypeOutbound(id: Option[Long],
//                               firstName: Option[String],
//                               lastName: Option[String],
//                               mobile: String,
//                               email: Option[String])

class MessageTypeTableDef(tag: Tag) extends Table[MessageTypeCase](tag, Some("talachitas_sms"), "message_type") {

  override def * =
    (id, name, description) <> (MessageTypeCase.tupled, MessageTypeCase.unapply)

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def name = column[Option[String]]("name")

  def description = column[Option[String]]("description")
}

class MessageType @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val messages = TableQuery[MessageTypeTableDef]

  def retrieveMessage(id: Long): Future[Option[MessageTypeCase]] = {
    db.run(messages.filter(mess => mess.id === id).map( messa =>
      (messa.id, messa.name, messa.description)
    ).result.map(
      _.headOption.map {
        case (id, name, description) =>
          MessageTypeCase(id, name, description)
      }
    ))
  }

}