package controllers

import formatter._
import javax.inject.Inject
import models.{ContactTable, Contacts}
import play.api.db.Database
import play.api.libs.json.{JsResult, JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


class ContactController @Inject()(cc: ControllerComponents, contactss: Contacts)
                                 (implicit context: ExecutionContext,
                                  database: Database,
                                  metrics: MetricsFacade) extends AbstractController(cc) {

  implicit val contactReader = ContactFormatter.ContactReader

  implicit val contactWriter = ContactFormatter.ContactWriter

  implicit val errorWriter = ErrorFormatter.errorWriter

  def listContacts = Action.async { implicit request =>
    contactss.listContacts map { contacts =>

      Ok(Json.toJson(contacts))
    }

  }

  def addContact = Action.async { implicit request =>

    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json =>

      val resultVal: JsResult[Contact] = json.validate[Contact]

      resultVal.asOpt.map { contactInboud =>

        val contactUser = ContactTable(
          contactInboud.email,
          contactInboud.subject,
          contactInboud.message)

        contactss.add(contactUser) map { contactOutbound =>

          Created(Json.toJson(contactOutbound))
        }
      } getOrElse {
        Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "new error"))))
      }
    } getOrElse {
      Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "Error"))))
    }
  }

}
