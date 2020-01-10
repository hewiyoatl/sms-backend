package controllers

import formatter._
import javax.inject.Inject
import models.{ContactTable, Contacts}
import play.api.db.Database
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


class ContactController @Inject()(cc: ControllerComponents, contactss: Contacts)
                                 (implicit context: ExecutionContext,
                                  database: Database,
                                  metrics: MetricsFacade) extends AbstractController(cc) {

  val EMAIL_SUCCESS_URL = "http://www.talachitas.com/html/english/contact-us-success.html"

  val EMAIL_FAILURE_URL = "http://www.talachitas.com/html/english/contact-us-error.html"

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
    val urlEncodedBody: Option[Map[String, Seq[String]]] = body.asFormUrlEncoded

    urlEncodedBody.map { encodedBody =>
      val emailOpt = encodedBody.get("email").map(_.mkString)
      val subjectOpt = encodedBody.get("subject").map(_.mkString)
      val messageOpt = encodedBody.get("message").map(_.mkString)
      val phoneNumberOpt = encodedBody.get("phone").map(_.mkString)

      emailOpt.map { email =>

        val contactUser = ContactTable(
          emailOpt, subjectOpt, messageOpt, phoneNumberOpt)

        contactss.add(contactUser) map { contactOutbound =>

          Redirect(EMAIL_SUCCESS_URL)
        }

      } getOrElse {

        Future(Redirect(EMAIL_FAILURE_URL))
      }

    } getOrElse {

      Future(Redirect(EMAIL_FAILURE_URL))
    }
  }

}
