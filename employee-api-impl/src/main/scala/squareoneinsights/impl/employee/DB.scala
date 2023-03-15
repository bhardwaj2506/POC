package squareoneinsights.impl.employee

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile


trait Db {
  val config: DatabaseConfig[JdbcProfile]
  val database: JdbcProfile#Backend#Database = config.db
}
