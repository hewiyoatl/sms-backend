package model

import com.google.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class User(id: Option[Long],
                firstName: Option[String],
                lastName: Option[String],
                mobile: String,
                email: Option[String],
                deleted: Boolean)

case class UserOutbound(id: Option[Long],
                        firstName: Option[String],
                        lastName: Option[String],
                        mobile: String,
                        email: Option[String])

case class UserFormData(firstName: String, lastName: String, mobile: String, email: String)

object UserForm {

  val form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "mobile" -> nonEmptyText,
      "email" -> email
    )(UserFormData.apply)(UserFormData.unapply)
  )
}

class UserTableDef(tag: Tag) extends Table[User](tag, Some("no_waiting"), "client") {

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def firstName = column[Option[String]]("first_name")

  def lastName = column[Option[String]]("last_name")

  def mobile = column[String]("phone_number")

  def email = column[Option[String]]("email")

  def deleted = column[Boolean]("deleted")

  override def * =
    (id, firstName, lastName, mobile, email, deleted) <>(User.tupled, User.unapply)
}

class Users @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val users = TableQuery[UserTableDef]

  def add(user: User): Future[Option[UserOutbound]] = {

    db.run(

      ((users returning users.map(_.id)) += user).flatMap(newId =>

        users.filter(u => u.id === newId && u.deleted === false).map(u =>

          (u.id, u.firstName, u.lastName, u.mobile, u.email)).result.map(_.headOption.map {

          case (id, firstName, lastName, mobile, email) =>
            UserOutbound(id, firstName, lastName, mobile, email)
        })

      ).transactionally)
  }

  def delete(id: Long): Future[Int] = {
    db.run(users.filter(_.id === id).map(u => u.deleted).update(true))
  }

  def listAll: Future[Seq[UserOutbound]] = {
    db.run(users.filter(_.deleted === false).map(u =>
      (u.id, u.firstName, u.lastName, u.mobile, u.email)).result.map(
        _.seq.map {
          case (id, firstName, lastName, mobile, email) =>
            UserOutbound(id, firstName, lastName, mobile, email)
        }
      )
    )
  }

  def retrieveUser(id: Long): Future[Option[UserOutbound]] = {
    db.run(users.filter(u => u.id === id && u.deleted === false).map(
      u => (u.id, u.firstName, u.lastName, u.mobile, u.email)).result.map(
        _.headOption.map {
          case (id, firstName, lastName, mobile, email) =>
            UserOutbound(id, firstName, lastName, mobile, email)
        }
      ))
  }

  def patchUser(user: User): Future[Option[UserOutbound]] = {

    db.run(

      users.filter(u => u.mobile === user.mobile && u.deleted === false).map(u => (u.firstName, u.lastName, u.email))
        .update(user.firstName, user.lastName, user.email)
        .flatMap(x =>

        users.filter(u => u.mobile === user.mobile && u.deleted === false).map(u =>
          (u.id, u.firstName, u.lastName, u.mobile, u.email)).result.map(_.headOption.map {
          case (id, firstName, lastName, mobile, email) => UserOutbound(id, firstName, lastName, mobile, email)

        })).transactionally)

  }

}