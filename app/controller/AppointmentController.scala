package controller

import model.{DoctorTable, Doctors}
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.mvc.{Action, Controller}

/**
  * Created by keerath on 31/10/17.
  */
class AppointmentController extends Controller {

  def listDoctorSpecialities() =  Action { request =>
    val specialities = Doctors.listSpecialities()
    Ok(JsObject(Seq("specialities" -> JsArray(specialities.map(JsString)))).toString())
  }

  def listDoctorsWithSpeciality(speciality: String) = Action { request =>
    val filteredDoctors = Doctors.filterWithSpeciality(speciality)
    implicit val writes = Json.writes[DoctorTable#TableElementType]
    Ok(JsObject(Seq("doctors" -> Json.toJson(filteredDoctors))))
  }

}
