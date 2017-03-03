package healthcheks

import java.sql.Connection
import javax.inject.Inject

import com.codahale.metrics.health.HealthCheck
import play.api.db.Database

class DatabaseHealthCheck @Inject()(database: Database) extends HealthCheck {
  override def check() = {
    val connection: Connection = database.getConnection()
    try {
      if (connection.isValid(5000)) {
        HealthCheck.Result.healthy()
      } else {
        HealthCheck.Result.unhealthy("Cannot connect to " + database.url)
      }
    } finally {
      try {
        if (connection != null)
          connection.close()
      } catch {
        case e: Exception =>
      }
    }
  }
}
