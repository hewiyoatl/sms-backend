package controllers

import auth.AuthAction
import formatter._
import javax.inject.Inject
import models.{ContactTable, Contacts}
import play.api.libs.json.Json
import play.api.mvc._
import utilities.Util

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


class ContactController @Inject()(cc: ControllerComponents, contactss: Contacts)
                                 (implicit context: ExecutionContext,
                                  metrics: MetricsFacade,
                                  authAction: AuthAction) extends AbstractController(cc) {

  val DEFAULT_LANGUAGE = "en"
  val ENGLISH_DOMAIN = "https://www.talachitas.com/talachitas/html/english/"
  val ENGLISH_SUCCESS_PAGE = "contact-us-success.html"
  val ENGLISH_FAILURE_PAGE = "contact-us-error.html"
  val ENGLISH_EMAIL_SUCCESS_URL = ENGLISH_DOMAIN + ENGLISH_SUCCESS_PAGE
  val ENGLISH_EMAIL_FAILURE_URL = ENGLISH_DOMAIN + ENGLISH_FAILURE_PAGE

  val SPANISH_DOMAIN = "https://www.talachitas.com/talachitas/html/spanish/"

  implicit val contactReader = ContactFormatter.ContactReader

  implicit val contactWriter = ContactFormatter.ContactWriter

  implicit val errorWriter = ErrorFormatter.errorWriter

  def ping = authAction.async { implicit request =>

    Future(Ok("Hello, Scala!"))
  }

  def listContacts = Action.async { implicit request =>
    contactss.listContacts map { contacts =>

      Ok(Json.toJson(contacts)).withHeaders(Util.headers: _*)
    }

  }

  private def redirectSuccess(languageOpt: Option[String]): Result = {

    languageOpt.map(lang =>
      if(lang == "sp")
        Redirect(ENGLISH_EMAIL_SUCCESS_URL)
      else
        Redirect(ENGLISH_EMAIL_SUCCESS_URL))
      .getOrElse(Redirect(ENGLISH_EMAIL_SUCCESS_URL))

  }

  private def redirectFailure(languageOpt: Option[String]): Result = {

    languageOpt.map(lang =>
      if(lang == "sp")
        Redirect(ENGLISH_EMAIL_FAILURE_URL)
      else
        Redirect(ENGLISH_EMAIL_FAILURE_URL))
      .getOrElse(Redirect(ENGLISH_EMAIL_FAILURE_URL))

  }

  def addContact = Action.async { implicit request =>

    val body: AnyContent = request.body
    val urlEncodedBody: Option[Map[String, Seq[String]]] = body.asFormUrlEncoded

    urlEncodedBody.map { encodedBody =>
      // we do not accept empty string
      val emailOpt = encodedBody.get("email").map(_.mkString).filter(_ != "")
      val subjectOpt = encodedBody.get("subject").map(_.mkString)
      val messageOpt = encodedBody.get("message").map(_.mkString)
      val phoneNumberOpt = encodedBody.get("phone").map(_.mkString)
      val languageOpt = encodedBody.get("language").map(_.mkString).orElse(Some(DEFAULT_LANGUAGE))

      emailOpt.map { email =>

        val contactUser = ContactTable(
          emailOpt, subjectOpt, messageOpt, phoneNumberOpt)

        contactss.add(contactUser) map { contactOutbound =>

          redirectSuccess(languageOpt)

        }

      } getOrElse {

        Future(redirectFailure(languageOpt))
      }

    } getOrElse {

      Future(redirectFailure(Some(DEFAULT_LANGUAGE)))
    }
  }

  def deleteContact(email: String) = authAction.async { implicit request =>

    contactss.deleteContact(email)
    Future(NoContent.withHeaders(Util.headers: _*))
  }

}
