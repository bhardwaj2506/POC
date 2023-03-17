package squareoneinsights.impl.db

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import org.slf4j.{Logger, LoggerFactory}
import squareoneinsights.api.models.Employee

import java.util.UUID
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Try


class CassandraEmployeeRepository(sessionCql: CqlSession)(implicit ec:ExecutionContextExecutor) extends EmployeeRepository {
  private final val log: Logger =
    LoggerFactory.getLogger(classOf[CassandraEmployeeRepository])

  def add(name: String): Future[Employee] = {
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

  override def get(id: Long): Future[Option[Employee]] = ???/*{

    val getEmployeeByIdStatement: PreparedStatement = sessionCql.prepare(
      s"SELECT id, name, department FROM my_keyspace.employee WHERE id = ?;")
//    val boundStatement = getEmployeeByIdStatement.bind(id)
//    val a= sessionCql.execute(boundStatement).map { row =>
//      Some(Employee(row.getString("name"), Some(row.getLong(("id")))))
//      Employee("",Some(1L))
//    }

    val boundStatement: BoundStatement = getEmployeeByIdStatement.bind()
      .setLong(0, id)
    val a = Try(sessionCql.executeAsync(boundStatement). { row =>
      Some(Employee("",Some(1L) ))
    }).toEither.left.map(err =>
      s"Error while inserting into cassandra for employee name  - ${err.getMessage}"
    )
    //Future.successful(Some(Employee("",Some(1L))))

    //getEmployeeByIdStatement.bind()

//    val boundStatement: BoundStatement = getEmployeeByIdStatement.bind()
//      //.setUuid("0L", id)
//    sessionCql.execute(boundStatement).map { resultSet =>
//      val row = resultSet.one()
//      if (row == null) None else Some(Employee(row.getUUID("id"), row.getString("name"), row.getString("department")))
//    }

  }*/

  override def update(employee: Employee): Future[Int] = ??? /*{
    val getEmployeeByIdStatement: PreparedStatement = sessionCql.prepare(
      s"SELECT id, name, department FROM my_keyspace.employee WHERE id = ?;")
    val bindUpdateProduct: BoundStatement = getEmployeeByIdStatement.bind()
    bindUpdateProduct.setLong("id", employee.id)
    bindUpdateProduct.setString("title", employee.name)
    Future.successful((bindUpdateProduct))
  }*/

  override def delete(id: Long): Future[Int] = ???

  override def listAll(): Future[Seq[Employee]] = ???
}
