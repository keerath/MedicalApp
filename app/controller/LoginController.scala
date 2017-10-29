package controller

import model.{DoctorTable, Doctors, PatientTable, Patients}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}


/**
  * Created by keerath on 29/10/17.
  */
class LoginController extends Controller {

  def patientLogin = Action { request =>
    request.body.asJson.flatMap { json =>
      val email = (json \ "email").as[String]
      val password = (json \ "password").as[String]
      implicit val writes = Json.writes[PatientTable#TableElementType]
      Patients.authenticate(email, password).map(x => Json.toJson(x).toString())
    }.map(Ok(_)).getOrElse(Unauthorized)
  }

  def doctorLogin = Action { request =>
    request.body.asJson.flatMap { json =>
      val email = (json \ "email").as[String]
      val password = (json \ "password").as[String]
      implicit val writes = Json.writes[DoctorTable#TableElementType]
      Doctors.authenticate(email, password).map(x => Json.toJson(x).toString())
    }.map(Ok(_)).getOrElse(Unauthorized)
  }

}
