package skuber

/**
  * @author David O'Riordan
  */
package object util {

  sealed trait DynamicResourceFailure // base trait for all failures related to loading and validating dynanmic resources

  sealed trait ValidationFailure extends DynamicResourceFailure
  case object NoKindSpecified extends ValidationFailure
  case object NoAPIVersionSpecified extends ValidationFailure
  case class UnsupportedType(kind: String, apiVersion: String) extends ValidationFailure
  case class ParseError(parseErr: String) extends ValidationFailure

  sealed trait LoadFailure extends DynamicResourceFailure
  case class FileDoesNotExist(path: String) extends LoadFailure
  case class NotAFile(path: String) extends LoadFailure
  case class ResourceLoadFailure(ex: Exception) extends LoadFailure

  type ValidationResult[O]=Either[ValidationFailure, O]
  type ValidatedResource=ValidationResult[skuber.ObjectResource]
  type LoadedAndValidatedResource=Either[DynamicResourceFailure, skuber.ObjectResource]
  type LoadedAndValidatedResources=Either[DynamicResourceFailure,Seq[skuber.ObjectResource]]
}
