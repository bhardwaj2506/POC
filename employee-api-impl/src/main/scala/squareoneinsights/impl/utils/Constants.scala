package squareoneinsights.impl.utils

object Constants {
  final val TOPIC = "topic"
  final val KAFKA_BOOTSTRAP_SERVERS = "kafkaBootstrapServer"

  final val INSERTION_FAILURE = "Error while inserting data into postgres database :"
  final val GETTING_FAILURE = "Error while getting data from postgres database : "
  final val UPDATE_FAILURE = "Error while updating data into postgres database :"
  final val DELETION_FAILURE = "Error while deleting data from postgres database :"
  final val FETCHING_ALL_FAILURE = "Error while fetching all the employee data from postgres database :"


  final val PRODUCING_FAILURE = "Error while producing employee records :"
  final val CONSUMING_FAILURE = "Error while consuming employees records from kafka consumer :"
  final val SEND_MESSAGE_FAILURE = "Error occurred while sending message to kafka consumer"

  final val MESSAGE_SENT_SUCCESSFULLY = "Message send successfully"

  final val INVALID_EMPLOYEE = "Employee Name cannot be empty"
  final val INVALID_DBTYPE = "DbType only have P for Postgres or C for Cassandra"

  final val EMPLOYEE_FOUND = "Employee found"
  final val EMPLOYEE_NOT_FOUND = "Employee not found"

  final val EMPLOYEE_UPDATED_SUCCESS = "Employee updated Successfully"


}
