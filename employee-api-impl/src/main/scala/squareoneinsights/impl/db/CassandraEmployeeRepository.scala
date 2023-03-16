package squareoneinsights.impl.db

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import org.slf4j.{Logger, LoggerFactory}
import squareoneinsights.api.models.Employee

import java.util.UUID
import scala.concurrent.Future
import scala.util.Try


class CassandraEmployeeRepository(sessionCql: CqlSession) extends EmployeeRepository {
  private final val log: Logger =
    LoggerFactory.getLogger(classOf[CassandraEmployeeRepository])

  def addEmployee(name: String): Future[Employee] = {
    val id: UUID = UUID.randomUUID()
    val employee = Employee(name)
   // sessionCql.execute(s"CREATE TABLE IF NOT EXISTS my_keyspace.employee(id BIGINT PRIMARY KEY, name TEXT")

    val insertStmt: PreparedStatement = sessionCql.prepare("INSERT INTO my_keyspace.employee(id, name) VALUES (?, ?)")
    val boundStmt: BoundStatement = insertStmt.bind(id, name)
    log.info(s"Adding employee data $employee to cassandra database")
    Try(sessionCql.execute(boundStmt)).toEither.left.map(err =>
      s"Error while inserting into cassandra for employee name $name - ${err.getMessage}"
    )
    Future.successful(employee)
  }
}
