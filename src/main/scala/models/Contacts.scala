package models

import com.google.inject.Inject
import formatter.Contact
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ContactTable(email: Option[String],
                        subject: Option[String],
                        message: Option[String])

class Contacts @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val contacts = TableQuery[ContactTableDef]

  def listContacts: Future[Seq[Contact]] = {
    db.run(contacts.map(contact =>
      (contact.email, contact.subject, contact.message)).result.map(
      _.seq.map {
        case (email, subject, message) =>
          Contact(email, subject, message)
      }
    ))
  }

  def add(contactUser: ContactTable): Future[Option[Contact]] = {

    db.run(
      (contacts += contactUser).transactionally)

    Future(Option(Contact(contactUser.email, None, None)))
  }

  class ContactTableDef(tag: Tag) extends Table[ContactTable](tag, Some("nowaiting"), "contact") {

    override def * =
      (email, subject, message) <> (ContactTable.tupled, ContactTable.unapply)

    def email = column[Option[String]]("email", O.PrimaryKey)

    def subject = column[Option[String]]("subject")

    def message = column[Option[String]]("message")
  }

}