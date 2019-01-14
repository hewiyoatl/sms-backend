package helpers

import play.api.{Configuration}

case class Auth0Config(secret: String, clientId: String, callbackURL: String, domain: String)

object Auth0Config {
  def get(config: Configuration) = {
    Auth0Config(
      config.get[String]("auth0.clientSecret"),
      config.get[String]("auth0.clientId"),
      config.get[String]("auth0.callbackURL"),
      config.get[String]("auth0.domain")
    )
  }
}
