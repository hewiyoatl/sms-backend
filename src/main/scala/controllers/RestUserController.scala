package controllers

import formatter._
import models.RestUser
import play.api.libs.json.{JsResult, JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RestUserController extends Controller {

  implicit val RestUserReader = RestUserFormatter.RestUserReader

  implicit val RestUserWriter = RestUserFormatter.RestUserWriter

  implicit val errorWriter = ErrorFormatter.errorWriter

  def listRestUsers = Action.async { implicit request =>
    RestUserFacade.listAllRestUsers map { restUsers =>

      Ok(Json.toJson(restUsers))
    }

  }

  def addRestUser = Action.async { implicit request =>

    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map {
      json =>

        val resultVal: JsResult[RestUserInbound] = json.validate[RestUserInbound]

        resultVal.asOpt.map { userInboud =>

          val newRestUser = RestUser(
            None,
            userInboud.sucursalId,
            userInboud.firstName,
            userInboud.lastName,
            userInboud.mobile,
            userInboud.userName,
            userInboud.password,
            false)

          RestUserFacade.addRestUser(newRestUser) map { restUser =>

            Created(Json.toJson(restUser))
          }
        } getOrElse {
          Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "new error"))))
        }
    } getOrElse {
      Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "Error"))))
    }
  }

  def deleteRestUser(id: Long) = Action.async { implicit request =>
    RestUserFacade.deleteRestUser(id)
    Future(NoContent)
  }

  def retrieveRestUser(id: Long) = Action.async { implicit request =>
    RestUserFacade.retrieveRestUser(id) map { restUser =>

      Ok(Json.toJson(restUser))
    }
  }

  def patchRestUser(id: Long) = Action.async { implicit request =>

    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map {
      json =>

        val resultVal: JsResult[RestUserInbound] = json.validate[RestUserInbound]

        resultVal.asOpt.map { restUserInboud =>

          val patchRestUser = RestUser(
            Some(id),
            restUserInboud.sucursalId,
            restUserInboud.firstName,
            restUserInboud.lastName,
            restUserInboud.mobile,
            restUserInboud.userName,
            restUserInboud.password,
            false)

          RestUserFacade.patchRestUser(patchRestUser) map { restUser =>

            Ok(Json.toJson(restUser))
          }
        } getOrElse {
          Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "new error"))))
        }
    } getOrElse {
      Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "Error"))))
    }
  }

}
