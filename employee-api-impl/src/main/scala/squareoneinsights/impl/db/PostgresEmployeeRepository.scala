package squareoneinsights.impl.db

import org.slf4j.{Logger, LoggerFactory}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import squareoneinsights.api.models.Employee
import squareoneinsights.impl.utils.Constants

import scala.concurrent.{ExecutionContext, Future}


class PostgresEmployeeRepository(val config: DatabaseConfig[JdbcProfile])(implicit val ec: ExecutionContext) extends EmployeeRepository with Db with EmployeeTrait {
  private final val log: Logger =
    LoggerFactory.getLogger(classOf[PostgresEmployeeRepository])

  import config.profile.api._

  val employeeTable = TableQuery[EmployeeTable]

  override def add(name: String): Future[Employee] = {
    val employee = Employee(name)
    val action = (employeeTable returning employeeTable.map(_.id) into ((emp, id) => emp.copy(id = Some(id)))) += employee
    log.info(s"Inserting employee details $employee into postgres database")
    database.run(action).recover {
      case ex: Exception =>
        log.error(s"Error while inserting data into postgres database : ${ex.getMessage}")
        throw new RuntimeException(Constants.INSERTION_FAILURE)
    }
  }

  override def get(id: Long): Future[Option[Employee]] = {
    database.run(employeeTable.filter(_.id === id).result.headOption).recover {
      case ex: Exception =>
        log.error(s"Error while getting data from postgres database : ${ex.getMessage}")
        throw new RuntimeException(Constants.GETTING_FAILURE)
    }
  }

  override def update(employee: Employee): Future[Int] = {
    database.run(employeeTable.filter(_.id === employee.id).update(employee)).recover {
      case ex: Exception =>
        log.error(s"Error while updating data into postgres database : ${ex.getMessage}")
        throw new RuntimeException(Constants.UPDATE_FAILURE)
    }
  }

  override def delete(id: Long): Future[Int] = {
    database.run(employeeTable.filter(_.id === id).delete).recover {
      case ex: Exception =>
        log.error(s"Error while deleting data from postgres database : ${ex.getMessage}")
        throw new RuntimeException(Constants.DELETION_FAILURE)
    }
  }

  override def listAll(): Future[Seq[Employee]] = {
    database.run(employeeTable.result).recover {
      case ex: Exception =>
        log.error(s"Error while fetching all the employee data from postgres database : ${ex.getMessage}")
        throw new RuntimeException(Constants.FETCHING_ALL_FAILURE)
    }
  }
}