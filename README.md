# Employee-CRUD-API

This is a RESTful API for managing employee data. It provides endpoints for creating, reading, updating, and deleting employees in a backend database.

## Technologies
* Scala 2.13
* Alpakka Cassandra 2.0.2
* Slick 3.3.3
* PostgreSQL 42.3.4

## Setup

1. Install PostgreSQL and create a database named employee_db.

2. Clone this repository and navigate to the project directory.

`git clone https://github.com/bhardwaj2506/POC.git
cd employee-crud-api`

3. Create a application.conf file in the src/main/resources directory with the following contents:

`akka {
loglevel = "INFO"
http {
server {
host = "0.0.0.0"
port = 9000
}
}
}

database {
driver = "org.postgresql.Driver"
url = "jdbc:postgresql://localhost:5432/employee_db"
user = "your-db-username"
password = "your-db-password"
}
`

Replace `your-db-username` and `your-db-password` with the credentials for your PostgreSQL database.

4. Run the following command to start the application:

`sbt runAll`

The application should now be accessible at http://localhost:9000

## Endpoints


### `GET http://localhost:9000/api/employee/get/{id}`

Returns the employee with the specified ID.

### `POST http://localhost:9000/api/employees/add`

Creates a new employee in the database. The employee data should be included in the request body in JSON format.

Example request body:
`{
"dbType" : "P",
"employeeName" : "Employee1"
}`

### `PUT http://localhost:9000/api/employee/update/{id}`

Updates the employee with the specified ID. The updated employee data should be included in the request body in JSON format.

Example request body:

`{
"id":1001,
"name":"Siddharth"
}`

### `DELETE http://localhost:9000/api/employee/delete/{id}`

Deletes the employee with the specified ID.