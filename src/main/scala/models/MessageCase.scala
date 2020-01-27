package models

import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import utilities.DateTimeMapper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MessageCase(id: Option[Long],
                       messageId: Option[String],
                       phoneNumber: Option[String],
                       createdTimestamp: Option[DateTime],
                       messageTypeId: Option[Long],
                       network: Option[String],
                       fromPhoneNumber: Option[String])

case class MessageOutbound(id: Option[Long],
                           firstName: Option[String],
                           lastName: Option[String],
                           mobile: String,
                           email: Option[String])

class MessageTableDef(tag: Tag) extends Table[MessageCase](tag, Some("talachitas_sms"), "messages") {

  override def * = (
    id,
    messageId,
    phoneNumber,
    createdTimestamp,
    messageTypeId,
    network,
    fromPhoneNumber) <> (MessageCase.tupled, MessageCase.unapply)

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def messageId = column[Option[String]]("message_id")

  def phoneNumber = column[Option[String]]("phone_number")

  def createdTimestamp = column[Option[DateTime]]("create_timestamp")

  def messageTypeId = column[Option[Long]]("message_type_id")

  def network = column[Option[String]]("network")

  def fromPhoneNumber = column[Option[String]]("from_phone_number")
}

class Message @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val messages = TableQuery[MessageTableDef]

  def add(message: MessageCase): Future[Option[MessageCase]] = {

    db.run(
      ((messages returning messages.map(_.id)) += message).flatMap(newId =>

        messages.filter(u => u.id === newId).map(u =>

          (u.id, u.messageId, u.phoneNumber, u.messageTypeId, u.network, u.fromPhoneNumber)).result.map(_.headOption.map {

          case (id, messageId, phoneNumber, messageTypeId, network, fromPhoneNumber) =>
            MessageCase(id, messageId, phoneNumber, None, messageTypeId, network, fromPhoneNumber)
        })

      ).transactionally)
  }

//  def delete(id: Long): Future[Int] = {
//    db.run(messages.filter(_.id === id).map(u => u.deleted).update(true))
//  }

//  def listAll: Future[Seq[MessageTypeOutbound]] = {
//    db.run(messages.filter(_.deleted === false).map(u =>
//      (u.id, u.firstName, u.lastName, u.mobile, u.email)).result.map(
//      _.seq.map {
//        case (id, firstName, lastName, mobile, email) =>
//          MessageTypeOutbound(id, firstName, lastName, mobile, email)
//      }
//    )
//    )
//  }

//  def retrieveClient(id: Long): Future[Option[MessageTypeOutbound]] = {
//    db.run(messages.filter(u => u.id === id && u.deleted === false).map(
//      u => (u.id, u.firstName, u.lastName, u.mobile, u.email)).result.map(
//      _.headOption.map {
//        case (id, firstName, lastName, mobile, email) =>
//          MessageTypeOutbound(id, firstName, lastName, mobile, email)
//      }
//    ))
//  }

//  def patchClient(client: MessageTypeCase): Future[Option[MessageTypeOutbound]] = {
//
//    db.run(
//
//      messages.filter(u => u.mobile === client.mobile && u.deleted === false).map(u => (u.firstName, u.lastName, u.email))
//        .update(client.firstName, client.lastName, client.email)
//        .flatMap(x =>
//
//          messages.filter(u => u.mobile === client.mobile && u.deleted === false).map(u =>
//            (u.id, u.firstName, u.lastName, u.mobile, u.email)).result.map(_.headOption.map {
//            case (id, firstName, lastName, mobile, email) => MessageTypeOutbound(id, firstName, lastName, mobile, email)
//
//          })).transactionally)
//
//  }

}