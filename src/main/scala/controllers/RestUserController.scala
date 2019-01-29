package controllers

import javax.inject.Inject

import formatter._
import models.{RestUser, RestUsers}
import play.api.db.Database
import play.api.libs.json.{JsResult, JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class RestUserController @Inject()(cc: ControllerComponents, restUsers: RestUsers)
                                  (implicit context: ExecutionContext,
                                   database: Database) extends AbstractController(cc) {

  implicit val RestUserReader = RestUserFormatter.RestUserReader

  implicit val RestUserWriter = RestUserFormatter.RestUserWriter

  implicit val errorWriter = ErrorFormatter.errorWriter

  def listRestUsers = Action.async { implicit request =>
    restUsers.listAll map { restUsers =>

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

          restUsers.add(newRestUser) map { restUser =>

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
    restUsers.delete(id)
    Future(NoContent)
  }

  def retrieveRestUser(id: Long) = Action.async { implicit request =>
    restUsers.retrieveRestUser(id) map { restUser =>

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

          restUsers.patchRestUser(patchRestUser) map { restUser =>

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
