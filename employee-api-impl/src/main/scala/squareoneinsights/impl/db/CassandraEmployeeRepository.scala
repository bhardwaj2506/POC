package squareoneinsights.impl.db

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import squareoneinsights.api.models.Employee

import java.util.UUID
import scala.concurrent.Future
import scala.util.Try


class CassandraEmployeeRepository(sessionCql: CqlSession) extends EmployeeRepository {

  def addEmployee(name: String): Future[Employee] = {
    val id: UUID = UUID.randomUUID()
    val employee = Employee(name)

    val insertStmt: PreparedStatement = sessionCql.prepare("INSERT INTO my_keyspace.employee(id, name) VALUES (?, ?)")
    val boundStmt: BoundStatement = insertStmt.bind(id, name)

    Try(sessionCql.execute(boundStmt)).toEither.left.map(err =>
      s"Error while inserting into cassandra for employee name $name - ${err.getMessage}"
    )
    //sessionCql.close()
    Future.successful(employee)
  }
}
