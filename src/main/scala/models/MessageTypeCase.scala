package models

import com.google.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MessageTypeCase(id: Option[Long],
                           name: Option[String],
                           description: Option[String],
                           keyword: Option[String],
                           status: Option[Long])

class MessageTypeTableDef(tag: Tag) extends Table[MessageTypeCase](tag, Some("talachitas_sms"), "message_type") {

  override def * =
    (id, name, description, keyword, status) <> (MessageTypeCase.tupled, MessageTypeCase.unapply)

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def name = column[Option[String]]("name")

  def description = column[Option[String]]("description")

  def keyword = column[Option[String]]("keyword")

  def status = column[Option[Long]]("status")
}

class MessageType @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val messages = TableQuery[MessageTypeTableDef]

  def retrieveMessage(id: Long): Future[Option[MessageTypeCase]] = {
    db.run(messages.filter(mess => mess.id === id).map( messa =>
      (messa.id, messa.name, messa.description, messa.keyword, messa.status)
    ).result.map(
      _.headOption.map {
        case (id, name, description, keyword, status) =>
          MessageTypeCase(id, name, description, keyword, status)
      }
    ))
  }

}