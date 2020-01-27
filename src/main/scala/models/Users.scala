package models

case class UserOutbound(id: Option[Long],
                        email: Option[String],
                        nickname: Option[String],
                        firstName: Option[String],
                        lastName: Option[String],
                        phoneNumber: Option[String],
                        roles: Option[List[String]],
                        bearerToken: Option[String])
