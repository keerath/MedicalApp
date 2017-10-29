package model

/**
  * Created by keerath on 29/10/17.
  */

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Try

case class Patient(id: Long, name: String, gender: String, email: String, password: String,
                   age: Int, bloodGroup: String, phoneNumber: String, emergencyNumber: String)

class PatientTable(tag: Tag) extends Table[Patient](tag, "Patient") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def gender = column[String]("gender")

  def email = column[String]("email")

  def password = column[String]("password")

  def age = column[Int]("age")

  def bloodGroup = column[String]("blood_group")

  def phoneNumber = column[String]("phone_number")

  def emergencyNumber = column[String]("emergency_number")

  override def * = (id, name, gender, email, password,
    age, bloodGroup, phoneNumber, emergencyNumber) <> ((Patient.apply _).tupled, Patient.unapply)
}

object Patients {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val patientTable = TableQuery[PatientTable]

  createTablesIfNotExist()

  def add(patient: Patient) = Await.result(dbConfig.db.run(patientTable += patient).map(res => patient), Duration.Inf)

  def authenticatePatient(email: String, password: String): Option[PatientTable#TableElementType] =
    Await.result(dbConfig.db.run(patientTable.filter(
      patient => patient.email === email && patient.password === password).result), Duration.Inf).headOption

  private def createTablesIfNotExist(): Unit = {
    val table = List(patientTable)
    val tableCreationFuture = dbConfig.db.run(DBIO.sequence(table.map(_.schema.create)))
    Try(Await.result(tableCreationFuture, Duration.Inf))
  }
}
