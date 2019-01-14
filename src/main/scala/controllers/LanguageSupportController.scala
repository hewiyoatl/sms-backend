package controllers

import javax.inject.Inject
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import utilities.Util

import scala.concurrent.ExecutionContext

class LanguageSupportController @Inject()(cc: ControllerComponents)
                                         (implicit context: ExecutionContext,
                                          database: Database,
                                          metrics: MetricsFacade,
                                          messagesApi: MessagesApi) extends AbstractController(cc) with I18nSupport  {
  def index = Action { request =>
    Ok(Util.languageSupport(messagesApi, "welcome.index", "usuario")(request))
  }
}