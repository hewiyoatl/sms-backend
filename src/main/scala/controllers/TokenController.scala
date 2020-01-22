package controllers


import javax.inject.Inject
import models.Contacts
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc._
import utilities.Util

import scala.concurrent.ExecutionContext


class TokenController @Inject()(cc: ControllerComponents, contactss: Contacts)
                               (implicit context: ExecutionContext,
                                wsClient: WSClient,
                                config: Configuration,
                                util: Util) extends AbstractController(cc) {

//  private def audience = config.get[String]("auth0.audience")
//
//  private def clientSecret = config.get[String]("auth0.clientSecret")
//
//  private def clientId = config.get[String]("auth0.clientId")
//
//  private def domain = config.get[String]("auth0.domain")
//
//  private def tokenUrl = s"https://$domain/oauth/token"

//  private def s = config.get[String]("auth.s")
//  private def x = config.get[String]("auth.x")
//  private def y = config.get[String]("auth.y")
//
//  val privateKey: PrivateKey = {
//
//    val S = BigInt(s, 16)
//    val curveParams = ECNamedCurveTable.getParameterSpec("P-521")
//    val curveSpec: ECParameterSpec = new ECNamedCurveSpec("P-521", curveParams.getCurve(), curveParams.getG(), curveParams.getN(), curveParams.getH());
//
//    val privateSpec = new ECPrivateKeySpec(S.underlying(), curveSpec)
//    val privateKeyEC = KeyFactory.getInstance("ECDSA", "BC").generatePrivate(privateSpec)
//
//    privateKeyEC
//
//  }
//
//  val publicKey: PublicKey = {
//
//    val X = BigInt(x, 16)
//    val Y = BigInt(y, 16)
//    val curveParams = ECNamedCurveTable.getParameterSpec("P-521")
//    val curveSpec: ECParameterSpec = new ECNamedCurveSpec(
//      "P-521",
//      curveParams.getCurve(),
//      curveParams.getG(),
//      curveParams.getN(),
//      curveParams.getH())
//
//    val publicSpec = new ECPublicKeySpec(new ECPoint(X.underlying(), Y.underlying()), curveSpec)
//
//    val publicKeyEC = KeyFactory.getInstance("ECDSA", "BC").generatePublic(publicSpec)
//
//    publicKeyEC
//
//  }

//  def provideToken = WithBasicAuth.async { implicit request =>
//
//    val jsonBodyString = s"""{"client_id":"$clientId","client_secret":"$clientSecret","audience":"$audience","grant_type":"client_credentials"}"""
//
//    wsClient.url(tokenUrl)
//      .addHttpHeaders(HeaderNames.CONTENT_TYPE -> "application/json")
//      .post(Json.parse(jsonBodyString)).map { response =>
//
//      if (response.status >= 200 && response.status < 300) {
//
//        Ok(response.json).withHeaders(util.headers: _*)
//      }
//      else {
//
//        InternalServerError("Error").withHeaders(util.headers: _*)
//      }
//
//    }
//
//  }

}
