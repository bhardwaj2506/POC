package squareoneinsights.impl.employee

import squareoneinsights.api.Employee


trait EmployeeTrait{
  this:Db =>
  import config.profile.api._

  class EmployeeTable(tag: Tag) extends Table[Employee](tag, "employee") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (name, id.?) <> (Employee.tupled, Employee.unapply)
  }
}