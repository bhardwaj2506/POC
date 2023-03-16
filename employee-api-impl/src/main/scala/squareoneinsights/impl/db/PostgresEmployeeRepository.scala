package squareoneinsights.impl.db

import org.slf4j.{Logger, LoggerFactory}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import squareoneinsights.api.models.Employee

import scala.concurrent.{ExecutionContext, Future}


class PostgresEmployeeRepository(val config: DatabaseConfig[JdbcProfile])(implicit val ec : ExecutionContext) extends EmployeeRepository with Db with EmployeeTrait {
  private final val log: Logger =
    LoggerFactory.getLogger(classOf[PostgresEmployeeRepository])

  import config.profile.api._

  val employeeTable = TableQuery[EmployeeTable]

  override def addEmployee(name: String): Future[Employee] = {
    val employee = Employee(name)
    val action = (employeeTable returning employeeTable.map(_.id) into ((emp, id) => emp.copy(id = Some(id)))) += employee
    log.info(s"Inserting employee details $employee into postgres database")
    database.run(action).recover {
      case ex: Exception =>
        log.error(s"Error while inserting data into postgres database : ${ex.getMessage}")
        throw new RuntimeException(s"Error while inserting data into postgres database : ${ex.getMessage}")
    }
  }
}