package utilities

import java.sql.Date

import org.joda.time.DateTime
import slick.jdbc.MySQLProfile.api._

object DateTimeMapper {

  implicit def date2dateTime = MappedColumnType.base[DateTime, Date](
    dateTime => new Date(dateTime.getMillis),
    date => new DateTime(date)
  )

}
