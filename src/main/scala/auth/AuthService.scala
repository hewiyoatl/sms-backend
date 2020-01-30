package auth

import java.security.spec.{ECParameterSpec, ECPoint, ECPublicKeySpec}
import java.security.{KeyFactory, PublicKey}

import javax.inject.Inject
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import pdi.jwt.{JwtAlgorithm, JwtBase64, JwtClaim, JwtJson}
import play.api.Configuration
import play.api.libs.json.Json
import services.EncryptDecryptService

import scala.util.{Failure, Success, Try}

class AuthService @Inject()(config: Configuration, encryptDecryptService: EncryptDecryptService) {

  private def x : String = encryptDecryptService.decrypt(config.get[String]("auth.x"))

  private def y = encryptDecryptService.decrypt(config.get[String]("auth.y"))

  private def s = encryptDecryptService.decrypt(config.get[String]("auth.s"))

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

  private val validateAdminClaims = (claims: JwtClaim) => {

    val roles: Option[String] = (Json.parse(claims.content) \ "roles").asOpt[String]
    val isAdmin: Boolean = roles.map(_.contains("Admin")).getOrElse(false)

    if (claims.expiration.get > System.currentTimeMillis && isAdmin) {

      Success(claims)
    }
    else {

      Failure(new Exception("Token expired"))
    }
  }

  // Validates a JWT and potentially returns the claims if the token was
  // successfully parsed and validated

  def validateAdminJwt(token: String): Try[JwtClaim] = for {

    claims <- JwtJson.decode(token, publicKey, Seq(JwtAlgorithm.ES512)) // Decode the token using the secret key

    _ <- validateAdminClaims(claims) // validate the data stored inside the token
  } yield claims


}