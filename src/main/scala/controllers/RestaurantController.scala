package controllers

import formatter._
import javax.inject.Inject
import models.Restaurant
import play.api.db.Database
import play.api.libs.json.{JsResult, JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


class RestaurantController @Inject()(cc: ControllerComponents)
                                    (implicit context: ExecutionContext,
                                     database: Database,
                                     metrics: MetricsFacade) extends AbstractController(cc) {

  implicit val restaurantReader = RestaurantFormatter.RestaurantReader

  implicit val restaurantWriter = RestaurantFormatter.RestaurantWriter

  implicit val errorWriter = ErrorFormatter.errorWriter

  def listRestaurants = Action.async { implicit request =>
    RestaurantFacade.listAllRestaurants map { restaurants =>

      Ok(Json.toJson(restaurants))
    }

  }

  def addRestaurant = Action.async { implicit request =>

    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map {
      json =>

        val resultVal: JsResult[RestaurantInbound] = json.validate[RestaurantInbound]

        resultVal.asOpt.map { restaurantInboud =>

          val newRestaurant = Restaurant(
            None,
            restaurantInboud.address1,
            restaurantInboud.address2,
            restaurantInboud.zipCode,
            restaurantInboud.state,
            restaurantInboud.city,
            restaurantInboud.country,
            restaurantInboud.phoneNumber,
            restaurantInboud.restaurantId,
            restaurantInboud.latitude,
            restaurantInboud.longitude,
            None,
            false)

          RestaurantFacade.addRestaurant(newRestaurant) map { user =>

            Created(Json.toJson(user))
          }
        } getOrElse {
          Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "Validation of the json file failed."))))
        }
    } getOrElse {
      Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "Error"))))
    }
  }

  def deleteRestaurant(id: Long) = Action.async { implicit request =>
    RestaurantFacade.deleteRestaurant(id)
    Future(NoContent)
  }

  def retrieveRestaurant(id: Long) = Action.async { implicit request =>
    RestaurantFacade.retrieveRetaurant(id) map { restaurant =>

      Ok(Json.toJson(restaurant))
    }
  }

  def patchRestaurant(id: Long) = Action.async { implicit request =>

    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map {
      json =>

        val resultVal: JsResult[RestaurantInbound] = json.validate[RestaurantInbound]

        resultVal.asOpt.map { restaurantInboud =>

          val patchRestaurant = Restaurant(
            Some(id),
            restaurantInboud.address1,
            restaurantInboud.address2,
            restaurantInboud.zipCode,
            restaurantInboud.state,
            restaurantInboud.city,
            restaurantInboud.country,
            restaurantInboud.phoneNumber,
            restaurantInboud.restaurantId,
            restaurantInboud.latitude,
            restaurantInboud.longitude,
            None,
            false)

          RestaurantFacade.patchRestaurant(patchRestaurant) map { restaurant =>

            Ok(Json.toJson(restaurant))
          }
        } getOrElse {
          Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "new error"))))
        }
    } getOrElse {
      Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "Error"))))
    }
  }

}
