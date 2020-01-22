package filters

import javax.inject.Inject
import akka.util.ByteString
import play.api.Logger
import play.api.libs.streams.Accumulator
import play.api.mvc._
import scala.concurrent.ExecutionContext

class LoggingFilter @Inject() (implicit ec: ExecutionContext) extends EssentialFilter {

  val logger: Logger = Logger(this.getClass())

  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {

      val startTime = System.currentTimeMillis

      val accumulator: Accumulator[ByteString, Result] = nextFilter(requestHeader)

      accumulator.map { result =>

        val endTime = System.currentTimeMillis
        val requestTime = endTime - startTime
        logger.info(s"${requestHeader.contentType} ${requestHeader.headers} ${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms and returned ${result.header.status}")

        logger.info(s"${requestHeader.contentType} ${requestHeader.headers} ${requestHeader.method} ${requestHeader.uri} response results ${result.header.headers} ")

        result.withHeaders("Request-Time" -> requestTime.toString)

      }
    }
  }
}