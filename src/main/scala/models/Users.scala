package models

import com.google.inject.Inject
import formatter.Contact
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class User(id: Option[Long],
                email: String,
                nickname: Option[String],
                password: String)

case class UserOutbound(id: Option[Long],
                        email: Option[String],
                        nickname: Option[String])

class Users @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val users = TableQuery[UsersTableDef]

//  def listContacts: Future[Seq[User]] = {
//    db.run(users.map(user =>
//      (user.email, user.nickname, user.password)).result.map(
//      _.seq.map {
//        case (email, nickname, password) =>
//          User(email, nickname, password)
//      }
//    ))
//  }

//  def add(user: User): Future[Option[User]] = {
//
//    db.run(
//      (users += user).transactionally)
//
//    Future(Option(User(user.email, Option(""), "")))
//  }


  def retrieveUser(email: String, password: String): Future[Option[UserOutbound]] = {

    db.run(users.filter(u => u.email === email && u.password === password).map(user =>
      (
        user.id,
        user.email,
        user.nickname
      )).result.map(
      _.headOption.map {
        case (
          id,
          email,
          nickname) =>
          UserOutbound(
            id,
            Option(email),
            nickname)
      }
    ))

  }

//  def deleteContact(email: String): Future[Int] = {
//
//    db.run(users.filter(_.email === email).delete)
//  }

  class UsersTableDef(tag: Tag) extends Table[User](tag, Some("nowaiting"), "users") {

    override def * =
      (id, email, nickname, password) <> (User.tupled, User.unapply)

    def id = column[Option[Long]]("id", O.PrimaryKey)

    def email = column[String]("email")

    def nickname = column[Option[String]]("nickname")

    def password = column[String]("password")
  }

}