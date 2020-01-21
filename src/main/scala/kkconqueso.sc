import java.security.spec.ECGenParameterSpec
import java.security.{KeyPairGenerator, SecureRandom, Security}

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.joda.time.DateTime
import pdi.jwt.{Jwt, JwtAlgorithm}

val ecGenSpec = new ECGenParameterSpec("P-521")
if (Security.getProvider("BC") == null) {
     Security.addProvider(new BouncyCastleProvider())
   }

val generatorEC = KeyPairGenerator.getInstance("ECDSA", "BC")

generatorEC.initialize(ecGenSpec, new SecureRandom())

val ecKey = generatorEC.generateKeyPair()

ecKey.getPrivate
ecKey.getPublic

//EC Private Key
//S: 1321d54cab0e6201577e0248c7ae0f24204cf99b8ed4c5615f8a5253e8d5a5934b7e2d7297837e11391e21222e8453f7d8dc5031ba00453a4d5617bb70d1e0c8c24
//  res3: java.security.PublicKey =
//EC Public Key
//X: 129d79758d409ed12c0edd72e1fb31b951faf2a306f66411fef268a4a2bb41d738b664e6468b7351ebc814fb15505dc1a3819b917e25f2a1672cb813a37b1200ec4
//  Y: 6bcd96bd15e455247745d259e0f72134012e557557e51070d807f0db486bb739c76bcc5ba41459377068339ca3b4d18a490d688b49c329d3466312993d54ce8f1c



val token = Jwt.encode("""{"user":1}""", ecKey.getPrivate, JwtAlgorithm.ES512)


new DateTime(1579601223466L)
