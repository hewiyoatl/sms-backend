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
import play.api.Configuration
import play.api.db.Database
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._

class Util @Inject()(config: Configuration) {

  /**
   * This variable is used to check the request param
   */
  val PRETTY = "pretty"

  val CALLER_INFO: String = "caller-info"

  val DOC_TEMPLATE_CONTEXT: String = "/docs"

  val DOC_INTERVIEW_CONTEXT: String = "/doc-ins"

  val DOC_HTML_GENERATOR_CONTEXT: String = "/doc-html"

  /**
   * Empty json
   */
  val EMPTY_JSON: String = "{}"

  val basicUser = "info@talachitas.com"

  val basicPassword = "Me gustan las chinas"

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

  def identityHeaderHardCode(implicit request: RequestHeader): Option[String] = {
    val apiHeader: String = s"""{"user":{"id":null,"accountMemberships":[]},"apiClient":null}"""
    val user: Option[String] = Some(apiHeader)
    user
  }

  def identityHeader(implicit request: RequestHeader): Option[String] = {
    val user: Option[String] = request.headers.get(CALLER_INFO)
    user
  }

  case class URLParts(urlProtocol: String, urlHost: String, urlPort: Int, urlPath: String)

  def getUrlParts(url: String): URLParts = {
    val urlExtract = new java.net.URL(url)
    URLParts(urlExtract.getProtocol, urlExtract.getHost, urlExtract.getPort, urlExtract.getPath)
  }

  /**
   * Expecting this string: path = /fr/fr/default.rl
   * Sending back this string: /fr/fr/interview#/{interview uuid}
   * @param path
   * @return
   */
  def removeRLPart(countryCode: String,
                   languageCode: String,
                   path: String,
                   interviewUuid: String,
                   templateUuid: String,
                   documentUuid: String): String = {
    val countryCodeLowerCase: String = countryCode.toLowerCase
    val languageCodeLowerCase: String = languageCode.toLowerCase
    val list: List[String] = path.split("/").toList
    if (list.size >= 2) {
      val removeEmptyString =
        if (list.headOption.getOrElse("") == "") {
          list.drop(1)
        } else {
          list
        }
      val param1: String = removeEmptyString.headOption.getOrElse(countryCodeLowerCase)
      val param2: String = removeEmptyString.tail.headOption.getOrElse(languageCodeLowerCase)
      s"""/$param1/$param2/interview/#/$interviewUuid?id=$templateUuid&document=$documentUuid"""
    } else s"""/$countryCodeLowerCase/$languageCodeLowerCase/interview/#/$interviewUuid?id=$templateUuid&document=$documentUuid"""
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

    val token = Jwt.encode(
      s"""{"email":"${user.email.getOrElse("")}",
         |"first_name":"${user.firstName.getOrElse("")}",
         |"last_name":"${user.lastName.getOrElse("")}",
         |"roles": ${user.roles.getOrElse(List())},
         |"exp": ${(new DateTime()).plusSeconds(expiration).getMillis},
         |"iat": ${System.currentTimeMillis()}}""".stripMargin,
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
