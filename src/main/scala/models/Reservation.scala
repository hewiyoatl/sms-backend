package models

import org.joda.time.DateTime
import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import utilities.MaybeFilter
import utilities.DateTimeMapper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Reservation(id: Option[Long],
                       userId: Option[Long],
                       userType: Option[Long],
                       locationId: Option[Long],
                       status: Option[Int],
                       created_timestamp: Option[DateTime])

case class ReservationOutbound(id: Option[Long],
                               userId: Option[Long],
                               userType: Option[Long],
                               locationId: Option[Long],
                               status: Option[Int],
                               createdTimestamp: Option[DateTime])

case class ReservationFormData(firstName: String, lastName: String, mobile: String, email: String)

object ReservationForm {

  val form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "mobile" -> nonEmptyText,
      "email" -> email
    )(ReservationFormData.apply)(ReservationFormData.unapply)
  )
}

class ReservationTableDef(tag: Tag) extends Table[Reservation](tag, "reservation") {

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

  def userId = column[Option[Long]]("user_id")

  def userType = column[Option[Long]]("user_type")

  def locationId = column[Option[Long]]("sucursal_id")

  def status = column[Option[Int]]("status")

  def createdTimestamp = column[Option[DateTime]]("created_timestamp")

  override def * =
    (id, userId, userType, locationId, status, createdTimestamp) <>(Reservation.tupled, Reservation.unapply)
}

object Reservations {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val reservations = TableQuery[ReservationTableDef]

  def add(reservation: Reservation): Future[Option[ReservationOutbound]] = {
    dbConfig.db.run((for {
      newId <- (reservations returning reservations.map(_.id)) += reservation
      a <- reservations.filter(reservation => reservation.id === newId).map(
        reservation => (
          reservation.id,
          reservation.userId,
          reservation.userType,
          reservation.locationId,
          reservation.status,
          reservation.createdTimestamp)).result.map(
          _.headOption.map {
            case (
              id,
              userId,
              userType,
              locationId,
              status,
              createdTimestamp
              ) =>
              ReservationOutbound(
                id,
                userId,
                userType,
                locationId,
                status,
                createdTimestamp
              )
          }
        )
    } yield a).transactionally)
  }

  def delete(id: Long): Future[Int] = {
    // TODO: WE NEED TO SET THE STATUS CODES. HERE 2 MEANS CANCCELLED
    dbConfig.db.run(reservations.filter(_.id === id).map(u => u.status).update(Some(2)))
  }

  def listAll(location: Option[Long]): Future[Seq[ReservationOutbound]] = {

    // TODO: WE NEED TO SET THE STATUS CODES. HERE 2 MEANS THAT IT IS CANCELLED.
    dbConfig.db.run(
      MaybeFilter(reservations)
        .filter(location)(v => d => d.locationId === v)
        .filter(Some(2))(v => d => d.status =!= v).query.map(reservation =>
        (
          reservation.id,
          reservation.userId,
          reservation.userType,
          reservation.locationId,
          reservation.status,
          reservation.createdTimestamp)).result.map(
          _.seq.map {
            case (id, userId, userType, locationId, status, createdTimestamp) =>
              ReservationOutbound(id, userId, userType, locationId, status, createdTimestamp)
          }
        )
    )
  }

  def retrieveReservation(id: Long): Future[Option[ReservationOutbound]] = {

    // TODO: WE NEED TO SET STATUS CODES
    dbConfig.db.run(reservations.filter(reservation => reservation.id === id && reservation.status =!= Option(2)).map(
      reservation => (
        reservation.id,
        reservation.userId,
        reservation.userType,
        reservation.locationId,
        reservation.status,
        reservation.createdTimestamp)).result.map(
        _.headOption.map {
          case (
            id, userId, userType, locationId, status, createdTimestamp) =>
            ReservationOutbound(id, userId, userType, locationId, status, createdTimestamp)
        }
      ))
  }

  def patchReservation(reservation: Reservation): Future[Option[ReservationOutbound]] = {

    dbConfig.db.run((for {
      _ <- reservations.filter(r =>
        r.id === reservation.id && r.status =!= Option(2)).map(r =>
        (r.status)).update(
          reservation.status
        )

      // TODO: SET THE CODE
      a <- reservations.filter(u => u.id === reservation.id && u.status =!= Option(2)).map(
        u => (u.id, u.userId, u.userType, u.locationId, u.status, u.createdTimestamp)).result.map(
          _.headOption.map {
            case (id, userId, userType, locationId, status, createdTimestamp) =>
              ReservationOutbound(id, userId, userType, locationId, status, createdTimestamp)
          }
        )

    } yield a).transactionally)
  }

}