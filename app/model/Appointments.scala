package model

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Try


case class Appointment(id: Long, patientId: Long, doctorId: Long, timestamp: Long)

case class AppointmentDetails(appointmentId: Long, appointmentTimestamp: Long,
                              name: String, gender: String, age: Int, email: String,
                              emergencyNumber: String, bloodGroup: String, doctorId: Long)

class AppointmentTable(tag: Tag) extends Table[Appointment](tag, "Appointment") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def patientId = column[Long]("patient_id")

  def patient = foreignKey("patient_fk", patientId, Patients.patientTable)(_.id)

  def doctorId = column[Long]("doctor_id")

  def doctor = foreignKey("doctor_fk", doctorId, Doctors.doctorTable)(_.id)

  def timestamp = column[Long]("timestamp")

  override def * = (id, patientId, doctorId, timestamp) <> ((Appointment.apply _).tupled, Appointment.unapply)
}

object Appointments {

  val db = DatabaseConfigProvider.get[JdbcProfile](Play.current).db
  val appointmentTable = TableQuery[AppointmentTable]

  createTableIfNotExist()

  private def createTableIfNotExist(): Unit = {
    val table = List(appointmentTable)
    val tableCreationFuture = db.run(DBIO.sequence(table.map(_.schema.create)))
    Try(Await.result(tableCreationFuture, Duration.Inf))
  }

  def add(appointment: Appointment) =
    Await.result(db.run(appointmentTable += appointment).map(res => appointment), Duration.Inf)

  val fatTable = for {

    ((appointment, patient), doctor) <- appointmentTable join Patients.patientTable on (_.patientId === _.id) join Doctors.doctorTable on (_._1.doctorId === _.id)

  } yield (appointment.id, appointment.timestamp, patient.name, patient.gender,
    patient.age, patient.email, patient.emergencyNumber, patient.bloodGroup, doctor.id)

  def list(doctorId: Long) = Await.result(db.run(fatTable.filter(_._9 === doctorId).sortBy(_._2).result), Duration.Inf)
    .map((AppointmentDetails.apply _).tupled)

  def get(appointmentId: Long) = (AppointmentDetails.apply _).tupled(
    Await.result(db.run(fatTable.filter(_._1 === appointmentId).result), Duration.Inf).head
  )
}
