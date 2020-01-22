package utilities

import java.security.spec.{ECParameterSpec, ECPrivateKeySpec}
import java.security.{KeyFactory, PrivateKey}

import com.kenshoo.play.metrics.Metrics
import com.zaxxer.hikari.HikariDataSource
import javax.inject.Inject
import models.UserOutbound
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.joda.time.DateTime
import pdi.jwt.{Jwt, JwtAlgorithm}
import play.api.{Configuration, Logger}
import play.api.db.Database
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._

class Util @Inject()(config: Configuration) {

  val logger: Logger = Logger(this.getClass())

  /**
   * This variable is used to check the request param
   */
  val PRETTY = "pretty"

  /**
   * Empty json
   */
  val EMPTY_JSON: String = "{}"

  val privateKey: PrivateKey = {

    val S = BigInt(s, 16)
    val curveParams = ECNamedCurveTable.getParameterSpec("P-521")
    val curveSpec: ECParameterSpec = new ECNamedCurveSpec(
      "P-521",
      curveParams.getCurve(),
      curveParams.getG(),
      curveParams.getN(),
      curveParams.getH())

    val privateSpec = new ECPrivateKeySpec(S.underlying(), curveSpec)
    import java.security.Security
    Security.addProvider(new BouncyCastleProvider)
    val privateKeyEC = KeyFactory.getInstance("ECDSA", "BC").generatePrivate(privateSpec)

    privateKeyEC

  }

  private def s = config.get[String]("auth.s")

  private def expiration = config.get[Int]("auth.expiration")

  /**
   * Utility method to validate that all the characters of a string are digits.
   *
   * @param value String to validate.
   * @return True if all characters are digits, false otherwise.
   */
  def isDigit(value: String): Boolean = value forall Character.isDigit

  /**
   * Related issues: https://groups.google.com/forum/#!topic/swagger-swaggersocket/TD0MWY09ESo
   * seems play filters still is in the works
   *
   * @param result
   * @return
   */
  def enableCors(result: Result) = result.withHeaders(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "PATCH, OPTIONS, GET, POST, PUT, DELETE, HEAD",
    "Access-Control-Allow-Headers" -> "Referrer, User-Agent, Cache-Control, Pragma, Date, Authorization, api_key, Accept, Content-Type, Origin, X-Json, X-Prototype-Version, X-Requested-With",
    "Access-Control-Allow-Credentials" -> "true",
    "Access-Control-Expose-Headers" -> "WWW-Authenticate, Server-Authorization, Location"
  )

  def initializeDBMetrics(db: Database, metrics: Metrics): Unit = {
    val dataSource : HikariDataSource = db.dataSource.asInstanceOf[HikariDataSource]
    if (dataSource.getMetricRegistry() == null) {
      dataSource.setMetricRegistry(metrics.defaultRegistry)
    }
  }

  def languageSupport(messagesApi: MessagesApi,
                      messageCode: String, param1: String)
                     (implicit request: RequestHeader): String = {
    val language: String =  request.acceptLanguages.map(_.code).headOption.getOrElse("en")
    messagesApi(messageCode, param1)(Lang(language))
  }

  def lines = scala.io.Source.fromFile("/opt/resources/play.properties").getLines().mkString

  case class URLParts(urlProtocol: String, urlHost: String, urlPort: Int, urlPath: String)

  def getUrlParts(url: String): URLParts = {
    val urlExtract = new java.net.URL(url)
    URLParts(urlExtract.getProtocol, urlExtract.getHost, urlExtract.getPort, urlExtract.getPath)
  }

  /**
    * value can be the password per see + a salt
    *
    * https://stackoverflow.com/questions/6840206/sha2-password-hashing-in-java
    *
    * @param value
    * @return
    */
  def getSha256(value: String) : String = {
    org.apache.commons.codec.digest.DigestUtils.sha256Hex(value)
  }

  def headers = List(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS, DELETE, PUT",
    "Access-Control-Max-Age" -> "3600",
    "Access-Control-Allow-Headers" -> "Origin, Content-Type, Accept, Authorization, access-control-allow-methods, access-control-allow-origin, access-control-allow-headers",
    "Access-Control-Allow-Credentials" -> "true"
  )

  def decodeBasicAuth(authHeader: String): (String, String) = {
    val baStr = authHeader.replaceFirst("Basic ", "")
    val decoded = new sun.misc.BASE64Decoder().decodeBuffer(baStr)
    val Array(user, password) = new String(decoded).split(":")
    (user, password)
  }

  def provideToken(user: UserOutbound): JsObject = {

    val message =      s"""{"email":"${user.email.getOrElse("")}",
                           |"first_name":"${user.firstName.getOrElse("")}",
                           |"last_name":"${user.lastName.getOrElse("")}",
                           |"roles": "${user.roles.getOrElse("")}",
                           |"exp": ${(new DateTime()).plusSeconds(expiration).getMillis},
                           |"iat": ${System.currentTimeMillis()}}""".stripMargin

    logger.info("Message to encode " + message)

    val token = Jwt.encode(message,
      privateKey,
      JwtAlgorithm.ES512)

    Json.obj(
      "email" -> user.email.map(JsString(_)),
      "first_name" -> user.firstName.map(JsString(_)),
      "last_name" -> user.lastName.map(JsString(_)),
      "roles" -> Json.toJson(user.roles.map(x => x).getOrElse(List())),
      "nickname" -> user.nickname.map(JsString(_)),
      "bearer_token" -> token)

  }
}
