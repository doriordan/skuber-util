package skuber.util

import cats.syntax.either._
import java.io.{File, FileInputStream, InputStreamReader}
import scala.io.Source

import play.api.libs.json.{Json,JsObject, JsSuccess,JsError}

/**
  * @author David O'Riordan
  *         Load Kuberenetes resources of unspecified types from files or other sources - formatted in Yaml or Json -
  *         and parse them into a Skuber model
  */
object LoadResources {

  private def loadYamlIntoJson(file: File): Either[ResourceLoadFailure, Seq[JsObject]] = {
    Either.catchOnly[java.io.IOException] {
      val fis = new FileInputStream(file)
      YamlReader.read(new InputStreamReader(fis))
    }.leftMap { ioException =>
      ResourceLoadFailure(ioException)
    }
  }

  private def jsonObjectsToValidatedResources(jsObjects: Seq[JsObject]) : LoadedAndValidatedResources = {
    val validatedAll: Seq[ValidatedResource] = jsObjects.map { js =>
      val dyn=new DynamicObjectResource(js)
      dyn.validated
    }
    // return a Failure if any single validation failed, otherwise return the set of skuber objects
    // TODO cleaner way to do following?
    val anyFailure=validatedAll.find( d => d.isLeft )
    anyFailure match {
      case Some(failed) => Either.left(failed.left.get)
      case None =>
        Either.right(
          validatedAll map { validated =>
            validated.right.get
          }
        )
    }
  }

  def loadYaml(file: File): LoadedAndValidatedResources = {
    if (!file.exists)
      Either.left(FileDoesNotExist(file.getAbsolutePath))
    else if (!file.isFile)
      Either.left(NotAFile(file.getAbsolutePath))
    else {
      val jsonObjects = loadYamlIntoJson(file)
      jsonObjects flatMap jsonObjectsToValidatedResources
    }
  }

  def loadJson(file: File): LoadedAndValidatedResource = {
    if (!file.exists)
      Either.left(FileDoesNotExist(file.getAbsolutePath))
    else if (!file.isFile)
      Either.left(NotAFile(file.getAbsolutePath))
    else {
      val jsonStr = Source.fromFile(file).getLines.mkString("\n")
      val jsonResult = Json.parse(jsonStr).validate[JsObject]
      jsonResult match {
        case JsSuccess(o, _) =>
          val dyn = new DynamicObjectResource(o)
          dyn.validated
        case JsError(err) => Either.left(ParseError(err.toString))
      }
    }
  }
}
