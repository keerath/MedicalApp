package model

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.Try

case class Patient(id: Long, name: String, password: String, email: String, age: Int, bloodGroup: String,
                   phoneNumber: String, emergencyNumber: String, gender: String)

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

  override def * = (id, name, password, email, age, bloodGroup,
    phoneNumber, emergencyNumber, gender) <> ((Patient.apply _).tupled, Patient.unapply)
}

object Patients {

  private val CSV_DELIM = """,(?=([^\"]*\"[^\"]*\")*[^\"]*$)"""
  val db = DatabaseConfigProvider.get[JdbcProfile](Play.current).db
  val patientTable = TableQuery[PatientTable]

  createTableIfNotExist()

  def add(patient: Patient) = Await.result(db.run(patientTable += patient).map(res => patient), Duration.Inf)

  def addAll(patients: List[Patient]) = Await.result(db.run(patientTable ++= patients), Duration.Inf)

  def authenticate(email: String, password: String): Option[PatientTable#TableElementType] =
    Await.result(db.run(patientTable.filter(
      patient => patient.email === email && patient.password === password).result), Duration.Inf).headOption

  private def parsePatientsFromCSV = {
    Source.fromFile(getClass.getClassLoader.getResource("patients.csv").getFile).getLines().toList.tail.map { line =>
      val Array(id, name, password, email, age, bloodGroup, phoneNumber, emergencyNumber, gender) = line
        .split(CSV_DELIM)
      Patient(id.toLong, name, password, email, age.toInt, bloodGroup, phoneNumber, emergencyNumber, gender)
    }
  }

  private def createTableIfNotExist(): Unit = {
    val table = List(patientTable)
    val tableCreationFuture = db.run(DBIO.sequence(table.map(_.schema.create)))

    if (Try(Await.result(tableCreationFuture, Duration.Inf)).isSuccess) {
      addAll(parsePatientsFromCSV)
    }
  }
}
