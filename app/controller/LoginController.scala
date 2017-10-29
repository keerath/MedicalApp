package controller

import model.{Patient, PatientTable, Patients}
import play.api.mvc.{Action, Controller}
import play.api.libs.json._


/**
  * Created by keerath on 29/10/17.
  */
class LoginController extends Controller {

  def patientLogin = Action { request =>
    request.body.asJson.flatMap { json =>
      val email = (json \ "email").as[String]
      val password = (json \ "password").as[String]
      implicit val writes = Json.writes[PatientTable#TableElementType]
      Patients.authenticatePatient(email, password).map(x => Json.toJson(x).toString())
    }.map(Ok(_)).getOrElse(Unauthorized)
  }

  def doctorLogin = Action {
    Ok("hello")
  }

}
