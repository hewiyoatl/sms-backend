package filters

import javax.inject.Inject
import play.api.mvc._
import scala.concurrent.ExecutionContext
import utilities.Util

class RLCorsFilter @Inject() (implicit ec: ExecutionContext, configuration: play.api.Configuration) extends EssentialFilter {

  def apply(next: EssentialAction) = new EssentialAction {

    def apply(requestHeader: RequestHeader) = {
      next(requestHeader).map { result =>
        Util.enableCors(result)
      }
    }
  }
}