/*
package squareoneinsights.impl.employee

package squareoneinsights.impl.employee

import akka.Done
import akka.actor.ActorSystem
import akka.persistence.cassandra.session.scaladsl.CassandraSession
import akka.stream.Materializer
import com.datastax.driver.core.{BoundStatement, Cluster}
import com.datastax.oss.driver.api.core.CqlSession

import java.sql.PreparedStatement
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


class CassandraEmployeeRepository(session: CqlSession, system: ActorSystem)(implicit mat: Materializer, ec: ExecutionContext) {

  private val cluster = Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9042)
    .build()

  private val preparedStatement: PreparedStatement = session.prepare(
    """INSERT INTO Employee (id, name) VALUES (?, ?)"""
  )

  def insertEmployee(employeeName: String): Future[Done] = {
    //val statement: BoundStatement = preparedStatement.bind()
    val statement =  session.execute(preparedStatement)
    statement.setString("id", java.util.UUID.randomUUID().toString)
    statement.setString("name", employeeName)

    val execution: Future[Done] = session.executeWrite(statement).map(_ => Done)

    execution.onComplete {
      case Success(_) =>
        println(s"Employee added to Cassandra")
      case Failure(ex) =>
        println(s"Error adding employee to Cassandra: ${ex.getMessage}")
    }
    execution
  }

  def close(): Unit = {
    cluster.close()
  }
}
*/
