package controllers

import auth.BasicAuthAction
import formatter._
import javax.inject.Inject
import models.Contacts
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import utilities.Util

import scala.concurrent.ExecutionContext


class TokenController @Inject()(cc: ControllerComponents, contactss: Contacts)
                               (implicit context: ExecutionContext,
                                wsClient: WSClient,
                                config: Configuration) extends AbstractController(cc) {

  private val WithBasicAuth = new BasicAuthAction(
    cc,
    Util.basicUser,
    Util.basicPassword)

  private def audience = config.get[String]("auth0.audience")

  private def clientSecret = config.get[String]("auth0.clientSecret")

  private def clientId = config.get[String]("auth0.clientId")

  private def domain = config.get[String]("auth0.domain")

  private def tokenUrl = s"https://$domain/oauth/token"


  def provideToken = WithBasicAuth.async { implicit request =>

    val jsonBodyString = s"""{"client_id":"$clientId","client_secret":"$clientSecret","audience":"$audience","grant_type":"client_credentials"}"""

    wsClient.url(tokenUrl)
      .addHttpHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      .post(Json.parse(jsonBodyString)).map { response =>

      if (response.status >= 200 && response.status < 300) {

        Ok(response.json).withHeaders(Util.headers: _*)
      }
      else {

        InternalServerError("Error").withHeaders(Util.headers: _*)
      }

    }

  }

}
