package controller

import model.{Doctor, Doctors, Patient, Patients}
import play.api.libs.json.Json
import play.api.mvc._

class RegistrationController extends Controller {

  def patientRegister = Action { request =>
    request.body.asJson.map { json =>
      implicit val writes = Json.writes[Patient]
      implicit val reads = Json.reads[Patient]
      Json.toJson(Patients.add(json.as[Patient])).toString()
    }.map(Ok(_)).getOrElse(BadRequest)
  }

  def doctorRegister = Action { request =>
    request.body.asJson.map { json =>
      implicit val writes = Json.writes[Doctor]
      implicit val reads = Json.reads[Doctor]
      Json.toJson(Doctors.add(json.as[Doctor])).toString()
    }.map(Ok(_)).getOrElse(BadRequest)
  }
}
