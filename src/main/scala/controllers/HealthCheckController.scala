package controllers

import java.util
import java.util.Map.Entry

import com.codahale.metrics.health.HealthCheck.Result
import com.codahale.metrics.health.HealthCheckRegistry
import com.kenshoo.play.metrics.MetricsDisabledException
import healthcheks.DatabaseHealthCheck
import io.prometheus.client.CollectorRegistry
import io.swagger.annotations._
import javax.inject.Inject
import play.api.Logger
import play.api.db._
import play.api.http.ContentTypes
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
 * Class to provide all the health checks related operations
 *
 * @param database
 * @param metrics
 */
@Api(value = "/health-check", protocols = "http, https")
class HealthCheckController @Inject()(cc: ControllerComponents)
                                     (implicit context: ExecutionContext,
                                      database: Database,
                                      metrics: MetricsFacade) extends AbstractController(cc) {

  val healthyMessage : String = " is healthy"

  val unhealthyMessage : String = " is not healthy"

  val healthChecks: HealthCheckRegistry = new HealthCheckRegistry

  /**
   * Register all the existing health checks
   */
  def registerHealthchecks : Unit = {
    healthChecks.register("postgres", new DatabaseHealthCheck(database))
  }

  registerHealthchecks

  /**
   * Method to run all the registered health checks
   *
   * @return
   */
  @ApiOperation(
    nickname = "runAllHealthChecks",
    value = "Method to run all the registered health checks",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Returns all the register health checks")
  ))
  def runAllHealthChecks = Action { request =>
    val results: util.SortedMap[String, Result] = healthChecks.runHealthChecks()
    val iterator: util.Iterator[Entry[String, Result]] = results.entrySet().iterator()
    var healthCheckString : String = ""
    while (iterator.hasNext) {
      val entry: Entry[String, Result] = iterator.next
      if (entry.getValue.isHealthy) {
        Logger.info(entry.getKey + healthyMessage)
        healthCheckString += entry.getKey + healthyMessage
      } else {
        val e: Throwable = entry.getValue.getError
        if (e != null) {
          Logger.error(entry.getKey + unhealthyMessage + entry.getValue.getMessage, e)
        } else {
          Logger.error(entry.getKey + unhealthyMessage + entry.getValue.getMessage)
        }
        healthCheckString += entry.getKey + unhealthyMessage + entry.getValue.getMessage
      }
    }
    Ok(healthCheckString)
  }

  /**
   * Method to capture the current metrics that the system is
   * providing via drop wizard metrics
   *
   * @return
   */
  @ApiOperation(
    nickname = "captureMetrics",
    value = "Method to capture the current metrics that the system is providing",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Provides the current via metrics dropwizard")
  ))
  def captureMetrics = Action {
    try {
      Ok(metrics.metricsPlay.toJson)
        .as("application/json")
        .withHeaders("Cache-Control" -> "must-revalidate,no-cache,no-store")
    } catch {
      case ex @ (_ : MetricsDisabledException | _ : Exception) =>
        InternalServerError("metrics plugin not enabled")
    }
  }

  /**
   * Method to capture the current metrics that the system is
   * providing via prometheus metrics
   *
   * @return
   */
  @ApiOperation(
    nickname = "captureMetricsPrometheus",
    value = "Method to capture the current metrics with prometheus format",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Provides the current metrics via prometheus format")
  ))
  def captureMetricsPrometheus = Action {
    val registry: CollectorRegistry = CollectorRegistry.defaultRegistry
    Ok(metrics.write004(registry.metricFamilySamples))
      .as(ContentTypes.TEXT)
  }

  @ApiOperation(
    nickname = "pong",
    value = "Ping / Pong communication",
    httpMethod = "GET",
    produces = "text/plain")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ping Pong Response successfully")))
  def pong = Action { request =>
    Logger.info("Ping Pong response ")
    Ok("Pong!").as(ContentTypes.TEXT)
  }

}
