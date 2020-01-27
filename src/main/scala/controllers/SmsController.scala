package controllers

import auth.AuthAction
import com.nexmo.client.NexmoClient
import com.nexmo.client.sms.MessageStatus
import com.nexmo.client.sms.messages.TextMessage
import formatter.{Error, ErrorFormatter}
import javax.inject.Inject
import models.{Message, MessageCase, MessageType}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{Configuration, Logger}
import utilities.Util

import scala.concurrent.{ExecutionContext, Future}


class SmsController @Inject()(cc: ControllerComponents,
                              config: Configuration,
                              util: Util,
                              authAction: AuthAction,
                              messageType: MessageType,
                              mes: Message)
                             (implicit context: ExecutionContext) extends AbstractController(cc) {

  implicit val errorWriter = ErrorFormatter.errorWriter

  val DEFAULT_MESSAGE = "A MESSAGE HAS BEEN SENT"

  val NEXMO_API_KEY: String = config.getOptional[String]("nexmo.key").getOrElse("")
  val NEXMO_API_SECRET: String = config.getOptional[String]("nexmo.secret").getOrElse("")
  val TO_NUMBER = "14155280256"
  val FROM_NUMBER = "14342330899"

  val client: NexmoClient = NexmoClient.builder.apiKey(NEXMO_API_KEY).apiSecret(NEXMO_API_SECRET).build

  def sendMessage = Action.async { implicit request =>

    val jsonBodyOpt = request.body.asJson

    jsonBodyOpt.map { json =>

      val messageTypeOpt = (json \ "message_type").asOpt[Long]
//      val messageOpt = (json \ "message").asOpt[String]
      val phoneNumberOpt = (json \ "phone_number").asOpt[String]

      phoneNumberOpt.map { phoneNumber =>

        messageTypeOpt.map { messageTypeVal =>

          val mTypeFut = messageType.retrieveMessage(messageTypeVal)

          mTypeFut.flatMap { mTypeOpt =>

            mTypeOpt.map { mType =>

              val finalMessage = new TextMessage(FROM_NUMBER, phoneNumber, mType.description.getOrElse(DEFAULT_MESSAGE))

              val response = client.getSmsClient.submitMessage(finalMessage)

              val messageResponse = response.getMessages.get(0)

              if (messageResponse.getStatus eq MessageStatus.OK) {

                mes.add(MessageCase(
                  None,
                  Some(messageResponse.getId),
                  phoneNumberOpt,
                  None,
                  messageTypeOpt,
                  Option(messageResponse.getNetwork),
                  Some(FROM_NUMBER)))

                System.out.println("Message sent successfully.")
                Future(Ok(s"""{"message_id": "${messageResponse.getId}"}"""))
              }
              else {

                System.out.println("Message failed with error: " + response.getMessages.get(0).getErrorText)

                Future(InternalServerError("There was a problem when sending the message"))
              }

            } getOrElse {

              Future(InternalServerError(Json.toJson(Error(INTERNAL_SERVER_ERROR, "No matching message type"))))
            }

          }

        } getOrElse {

          Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "no message"))))
        }

      } getOrElse {

        Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "no phone number"))))
      }


    } getOrElse {

      Future(BadRequest(Json.toJson(Error(BAD_REQUEST, "not well-formed"))))
    }

  }

  def receiveMessage = Action.async { implicit request =>

    val misdn =request.getQueryString("msisdn").getOrElse("No value")
    val messageId = request.getQueryString("messageId").getOrElse("No value")
    val text = request.getQueryString("text").getOrElse("No value")
    val typeVal = request.getQueryString("type").getOrElse("No value")
    val keywork = request.getQueryString("keyword").getOrElse("No VAlue")
    val messageTimestamp = request.getQueryString("message-timestamp").getOrElse("no value")

    Logger.info(s"@@@$misdn@@@ @@@$messageId@@@ @@@$text@@@ @@@$typeVal@@@ @@@$keywork@@@ @@@$messageTimestamp@@@")

    Future(Ok(s"messageid @@@${messageId}@@@ @@@${text}@@@"))

  }

}
