package squareoneinsights.impl.employee

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import squareoneinsights.api.Employee

import scala.concurrent.Future


class PostgresEmployeeRepository(val config: DatabaseConfig[JdbcProfile]) extends EmployeeRepository with Db with EmployeeTrait {

  import config.profile.api._


  val employeeTable = TableQuery[EmployeeTable]

  override def addEmployee(name: String): Future[Employee] = {
    val employee = Employee(name)
    val action = (employeeTable returning employeeTable.map(_.id) into ((emp, id) => emp.copy(id = Some(id)))) += employee
    database.run(action)
  }
}