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
                       fromPhoneNumber: Option[String],
                       keyword: Option[String],
                       status: Option[Long])

//case class MessageOutbound(id: Option[Long],
//                           firstName: Option[String],
//                           lastName: Option[String],
//                           mobile: String,
//                           email: Option[String])

class MessageTableDef(tag: Tag) extends Table[MessageCase](tag, Some("talachitas_sms"), "messages") {

  override def * = (
    id,
    messageId,
    phoneNumber,
    createdTimestamp,
    messageTypeId,
    network,
    fromPhoneNumber,
    keyword,
    status) <> (MessageCase.tupled, MessageCase.unapply)

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def messageId = column[Option[String]]("message_id")

  def phoneNumber = column[Option[String]]("phone_number")

  def createdTimestamp = column[Option[DateTime]]("create_timestamp")

  def messageTypeId = column[Option[Long]]("message_type_id")

  def network = column[Option[String]]("network")

  def fromPhoneNumber = column[Option[String]]("from_phone_number")

  def keyword = column[Option[String]]("keyword")

  def status = column[Option[Long]]("status")
}

class Message @Inject()(val dbConfigProvider: DatabaseConfigProvider,
                        customizedSlickConfig: CustomizedSlickConfig) extends HasDatabaseConfigProviderTalachitas[JdbcProfile] {

  this.dbConfig = customizedSlickConfig.createDbConfigCustomized(dbConfigProvider)

  val messages = TableQuery[MessageTableDef]

  def add(message: MessageCase): Future[Option[MessageCase]] = {

    db.run(
      ((messages returning messages.map(_.id)) += message).flatMap(newId =>

        messages.filter(u => u.id === newId).map(u =>
          (
            u.id,
            u.messageId,
            u.phoneNumber,
            u.messageTypeId,
            u.network,
            u.fromPhoneNumber,
            u.keyword,
            u.status)).result.map(_.headOption.map {

          case (id, messageId, phoneNumber, messageTypeId, network, fromPhoneNumber, keyword, status) =>
            MessageCase(id, messageId, phoneNumber, None, messageTypeId, network, fromPhoneNumber,keyword, status)
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

  def retrieveMessage(toPhone: String, keyword: String): Future[Option[MessageCase]] = {

    db.run(messages.filter(incomingMessage =>

      incomingMessage.phoneNumber.getOrElse("") === toPhone &&
        incomingMessage.keyword.getOrElse("") === keyword.toUpperCase).map(m =>

      (m.id,
        m.messageId,
        m.phoneNumber,
        m.createdTimestamp,
        m.messageTypeId,
        m.network,
        m.fromPhoneNumber,
        m.keyword,
        m.status)).sortBy(res => res._1.desc).result.map(_.headOption.map {

      case (
        id,
        messageId,
        phoneNumber,
        createdTimestamp,
        messageTypeId,
        network,
        fromPhoneNumber,
        keyword,
        status) =>
        MessageCase(
          id,
          messageId,
          phoneNumber,
          createdTimestamp,
          messageTypeId,
          network,
          fromPhoneNumber,
          keyword,
          status)
    }).transactionally)
  }

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