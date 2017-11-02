package controller

import model.{Appointment, AppointmentDetails, AppointmentTable, Appointments, DoctorTable, Doctors, Patient}
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString, Json}
import play.api.mvc.{Action, Controller}


class AppointmentController extends Controller {

  def listDoctorSpecialities() = Action { request =>
    val specialities = Doctors.listSpecialities()
    Ok(JsObject(Seq("specialities" -> JsArray(specialities.map(JsString)))).toString())
  }

  def listDoctorsWithSpeciality(speciality: String) = Action { request =>
    val filteredDoctors = Doctors.filterWithSpeciality(speciality)
    implicit val writes = Json.writes[DoctorTable#TableElementType]
    Ok(JsObject(Seq("doctors" -> Json.toJson(filteredDoctors))))
  }

  def bookAppointment = Action { request =>
    request.body.asJson.map { json =>
      implicit val reads = Json.reads[Appointment]
      val appointment = json.as[Appointment]
      implicit val writes = Json.writes[Appointment]
      Ok(Json.toJson(Appointments.add(appointment)).toString())
    }.getOrElse(BadRequest)
  }

  def listAppointments(doctorId: Long) = Action { request =>
    val appointmentDetailsList = Appointments.list(doctorId.toLong)
    implicit val writes = Json.writes[AppointmentDetails]
    Ok(Json.toJson(appointmentDetailsList).toString())
  }

  def getDetails(appointmentId: Long) = Action { request =>
    val details = Appointments.get(appointmentId)
    implicit val writes = Json.writes[AppointmentDetails]
    Ok(Json.toJson(details).toString())
  }
}
