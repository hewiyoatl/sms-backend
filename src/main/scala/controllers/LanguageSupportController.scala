package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import utilities.Util

class LanguageSupportController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport  {

  def index = Action { request =>
    Ok(Util.languageSupport(messagesApi, "welcome.index", "usuario")(request))
  }
}