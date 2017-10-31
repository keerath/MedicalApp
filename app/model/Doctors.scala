package model

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Try

case class Doctor(id: Long, name: String, password: String, email: String, phoneNumber: String,
                  qualifications: String, speciality: String, hospitalName: String)

class DoctorTable(tag: Tag) extends Table[Doctor](tag, "Doctor") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def password = column[String]("password")

  def email = column[String]("email")

  def phoneNumber = column[String]("phone_number")

  def qualifications = column[String]("qualifications")

  def speciality = column[String]("speciality")

  def hospitalName = column[String]("hospital_name")

  override def * = (id, name, password, email, phoneNumber, qualifications,
    speciality, hospitalName) <> ((Doctor.apply _).tupled, Doctor.unapply)
}

object Doctors {

  val db = DatabaseConfigProvider.get[JdbcProfile](Play.current).db

  val doctorTable = TableQuery[DoctorTable]

  createTableIfNotExist()

  def add(doctor: Doctor) = Await.result(db.run(doctorTable += doctor).map(res => doctor), Duration.Inf)

  def authenticate(email: String, password: String): Option[DoctorTable#TableElementType] =
    Await.result(db.run(doctorTable.filter(
      doctor => doctor.email === email && doctor.password === password).result), Duration.Inf).headOption

  private def createTableIfNotExist(): Unit = {
    val table = List(doctorTable)
    val tableCreationFuture = db.run(DBIO.sequence(table.map(_.schema.create)))
    Try(Await.result(tableCreationFuture, Duration.Inf))
  }

  def listSpecialities() =
    Await.result(db.run(doctorTable.map(_.speciality).distinct.result), Duration.Inf)

  def filterWithSpeciality(speciality: String) =
    Await.result(db.run(doctorTable.filter(_.speciality === speciality).result), Duration.Inf)
}

