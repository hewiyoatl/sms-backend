package controllers

import formatter._
import javax.inject.Inject
import models.Reservation
import play.api.db.Database
import play.api.libs.json.{JsResult, JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


class ReservationController @Inject()(cc: ControllerComponents)
                                     (implicit context: ExecutionContext,
                                      database: Database,
                                      metrics: MetricsFacade) extends AbstractController(cc) {

  implicit val reservationReader = ReservationFormatter.ReservationReader

  implicit val reservationWriter = ReservationFormatter.ReservationWriter

  implicit val errorWriter = ErrorFormatter.errorWriter

  def listReservations(location: Option[Long]) = Action.async { implicit request =>
    ReservationFacade.listAllReservations(location) map { reservations =>

      Ok(Json.toJson(reservations))
    }

  }

  def addReservation = Action.async { implicit request =>

    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map {
      json =>

        val resultVal: JsResult[ReservationInbound] = json.validate[ReservationInbound]

        resultVal.asOpt.map { reservationInboud =>

          val reservationUser = Reservation(
            None,
            reservationInboud.userId,
            reservationInboud.userType,
            reservationInboud.sucursalId,
            Some(1),
            None)

          ReservationFacade.addReservation(reservationUser) map { reservation =>

            Created(Json.toJson(reservation))
          }
        } getOrElse {
          Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "new error"))))
        }
    } getOrElse {
      Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "Error"))))
    }
  }

  def deleteReservation(id: Long) = Action.async { implicit request =>
    ReservationFacade.deleteReservation(id)
    Future(NoContent)
  }

  def retrieveReservation(id: Long) = Action.async { implicit request =>
    ReservationFacade.retrieveReservation(id) map { reservation =>

      Ok(Json.toJson(reservation))
    }
  }

  def patchReservation(id: Long) = Action.async { implicit request =>

    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map {
      json =>

        val resultVal: JsResult[ReservationInbound] = json.validate[ReservationInbound]

        resultVal.asOpt.map { reservationInboud =>

          val reservationReservation = Reservation(
            Some(id),
            reservationInboud.userId,
            reservationInboud.userType,
            reservationInboud.sucursalId,
            reservationInboud.status,
            None)

          ReservationFacade.patchReservation(reservationReservation) map { reservation =>

            Ok(Json.toJson(reservation))
          }
        } getOrElse {
          Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "new error"))))
        }
    } getOrElse {
      Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "Error"))))
    }
  }

}
