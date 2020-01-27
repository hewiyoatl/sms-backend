package controllers

import auth.BasicAuthAction
import javax.inject.Inject
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import utilities.Util

import scala.concurrent.ExecutionContext


class TokenController @Inject()(cc: ControllerComponents)
                               (implicit context: ExecutionContext,
                                wsClient: WSClient,
                                config: Configuration,
                                withBasicAuth: BasicAuthAction,
                                util: Util) extends AbstractController(cc) {

  def provideToken = withBasicAuth.async { implicit request =>

//    val jsonBodyString = s"""{"client_id":"$clientId","client_secret":"$clientSecret","audience":"$audience","grant_type":"client_credentials"}"""
    val jsonBodyString = ""
//    util.provideToken()

    wsClient.url("")
      .addHttpHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
      .post(Json.parse(jsonBodyString)).map { response =>

      if (response.status >= 200 && response.status < 300) {

        Ok(response.json).withHeaders(util.headers: _*)
      }
      else {

        InternalServerError("Error").withHeaders(util.headers: _*)
      }

    }

  }

}
