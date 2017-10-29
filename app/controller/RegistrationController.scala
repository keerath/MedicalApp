package controller

import model.{Patient, PatientTable, Patients}
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

  def doctorRegister = Action {
    Ok("hello")
  }
}
