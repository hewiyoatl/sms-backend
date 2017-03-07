package controllers

import models.{Reservation, ReservationOutbound, Reservations}

import scala.concurrent.Future

object ReservationFacade {

  def addReservation(reservation: Reservation): Future[Option[ReservationOutbound]] = {
    Reservations.add(reservation)
  }

  def deleteReservation(id: Long): Future[Int] = {
    Reservations.delete(id)
  }

  def listAllReservations(location: Option[Long]): Future[Seq[ReservationOutbound]] = {
    Reservations.listAll(location)
  }

  def retrieveReservation(id: Long): Future[Option[ReservationOutbound]] = {
    Reservations.retrieveReservation(id)
  }

  def patchReservation(reservation: Reservation): Future[Option[ReservationOutbound]] = {
    Reservations.patchReservation(reservation)
  }

}
