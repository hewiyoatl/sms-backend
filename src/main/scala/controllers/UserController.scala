package controllers

import formatter.{Error, ErrorFormatter}
import javax.inject.Inject
import models.Users
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import play.mvc.Http.HeaderNames
import utilities.Util

import scala.concurrent.{ExecutionContext, Future}


class UserController @Inject()(cc: ControllerComponents, users: Users)
                              (implicit context: ExecutionContext) extends AbstractController(cc) {

  implicit val errorWriter = ErrorFormatter.errorWriter

  def profile = Action.async { request =>

    request.headers.get(HeaderNames.AUTHORIZATION) map { basicHeader =>

      val (user, password) = Util.decodeBasicAuth(basicHeader)
      Logger(s"""@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@user: ${user}, password: ${password}""")

      users.retrieveUser(user, password) map { userOpt =>

        userOpt map { user =>

          Ok(Json.obj("user_id" -> user.id, "nickname" -> user.nickname, "email" -> user.email))

        } getOrElse (Forbidden(Json.toJson(Error(FORBIDDEN, "Unauthorized user"))))

      }

    } getOrElse {

      Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "Missing authorization header"))))
    }

  }

}
