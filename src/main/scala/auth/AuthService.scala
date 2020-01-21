package auth

import java.security.spec.{ECParameterSpec, ECPoint, ECPrivateKeySpec, ECPublicKeySpec}
import java.security.{KeyFactory, PrivateKey, PublicKey}

import javax.inject.Inject
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import pdi.jwt.{JwtAlgorithm, JwtBase64, JwtClaim, JwtJson}
import play.api.Configuration
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

class AuthService @Inject()(config: Configuration) {

  val publicKey: PublicKey = {

    val X = BigInt(x, 16)
    val Y = BigInt(y, 16)
    val curveParams = ECNamedCurveTable.getParameterSpec("P-521")
    val curveSpec: ECParameterSpec = new ECNamedCurveSpec(
      "P-521",
      curveParams.getCurve(),
      curveParams.getG(),
      curveParams.getN(),
      curveParams.getH())

    val publicSpec = new ECPublicKeySpec(new ECPoint(X.underlying(), Y.underlying()), curveSpec)

    import java.security.Security
    Security.addProvider(new BouncyCastleProvider)
    val publicKeyEC = KeyFactory.getInstance("ECDSA", "BC").generatePublic(publicSpec)

    publicKeyEC

  }

  // A regex that defines the JWT pattern and allows us to
  // extract the header, claims and signature
  private val jwtRegex = """(.+?)\.(.+?)\.(.+?)""".r

  // Splits a JWT into it's 3 component parts
  private val splitToken = (jwt: String) => jwt match {
    case jwtRegex(header, body, sig) => Success((header, body, sig))
    case _ => Failure(new Exception("Token does not match the correct pattern"))
  }

  // .. add the new methods below 'validateJwt'
  // As the header and claims data are base64-encoded, this function
  // decodes those elements
  private val decodeElements = (data: Try[(String, String, String)]) => data map {

    case (header, body, sig) =>
      (JwtBase64.decodeString(header), JwtBase64.decodeString(body), sig)
  }

  // Gets the JWK from the JWKS endpoint using the jwks-rsa library
  //  private val getJwk = (token: String) =>
  //    (splitToken andThen decodeElements) (token) flatMap {
  //      case (header, _, _) =>
  //        val jwtHeader = JwtJson.parseHeader(header) // extract the header
  //      val jwkProvider = new UrlJwkProvider(s"https://$domain")
  //
  //        // Use jwkProvider to load the JWKS data and return the JWK
  //        jwtHeader.keyId.map { k =>
  //          Try(jwkProvider.get(k))
  //        } getOrElse Failure(new Exception("Unable to retrieve kid"))
  //    }
  private val validateAdminClaims = (claims: JwtClaim) => {

    val isAdmin: Boolean = (Json.toJson(claims.content) \ "roles").asOpt[List[String]]
      .map(_.contains("Admin")).getOrElse(false)

    if (claims.expiration.get > System.currentTimeMillis && isAdmin) {

      Success(claims)
    }
    else {

      Failure(new Exception("Token expired"))
    }
  }

  private val validateUserClaims = (claims: JwtClaim) => {

    val isUser: Boolean = (Json.toJson(claims.content) \ "roles").asOpt[List[String]]
      .map(_.contains("User")).getOrElse(false)

    if (claims.expiration.get > System.currentTimeMillis && isUser) {

      Success(claims)
    }
    else {

      Failure(new Exception("Token expired"))
    }
  }

  // Validates a JWT and potentially returns the claims if the token was
  // successfully parsed and validated
  def validateUserJwt(token: String): Try[JwtClaim] = for {

    //    jwk <- getJwk(token) // Get the secret key for this token

    claims <- JwtJson.decode(token, publicKey, Seq(JwtAlgorithm.ES512)) // Decode the token using the secret key

    _ <- validateUserClaims(claims) // validate the data stored inside the token
  } yield claims

  def validateAdminJwt(token: String): Try[JwtClaim] = for {

    //    jwk <- getJwk(token) // Get the secret key for this token

    claims <- JwtJson.decode(token, publicKey, Seq(JwtAlgorithm.ES512)) // Decode the token using the secret key

    _ <- validateAdminClaims(claims) // validate the data stored inside the token
  } yield claims

//  private def s = config.get[String]("auth.s")

  private def x = config.get[String]("auth.x")

  private def y = config.get[String]("auth.y")

  // Your Auth0 audience, read from configuration
  //  private def audience = config.get[String]("auth0.audience")

  // The issuer of the token. For Auth0, this is just your Auth0
  // domain including the URI scheme and a trailing slash.
  //  private def issuer = s"https://$domain/"

  // Your Auth0 domain, read from configuration
  //  private def domain = config.get[String]("auth0.domain")

}