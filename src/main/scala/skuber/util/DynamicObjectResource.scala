package skuber.util

import play.api.libs.json._
import cats.syntax.either._
import play.api.data.validation.ValidationError


// import all the Skuber Json implicit formatters to support reading any supported type
import skuber.json.format._
import skuber.json.ext.format._
import skuber.json.apps.format._
import skuber.json.batch.format._
import skuber.json.rbac.format._
import skuber.json.annotation.format._

import skuber.ext.{Deployment => ExtDeployment, ReplicaSet, Ingress, DaemonSet}
import skuber.apps.{Deployment => AppsDeployment, StatefulSet}
import skuber.rbac.{RoleBinding, ClusterRoleBinding, ClusterRole, Role}
import skuber.batch.{Job, CronJob}

/**
  * @author David O'Riordan
  * Handles Kubernetes resources where we don't know the static type but have the JSON representation.
  * Can be used to load arbitrary resources into a Skuber model.
  */
class DynamicObjectResource(js: JsObject) {

  // workaround issue whereby some formatters imported from skuber are not found by the implicit
  // resolution mechanism here, possibly because they are declared as lazy vals (?)
  implicit val extDeplFormat=skuber.json.ext.format.depFormat
  implicit val appsDeplFormat=skuber.json.apps.format.depFormat

  // Public API to retrieve the validated details of the type
  // The return type for each is a cats Either, so right biased. If set to left, it contains a validation failure.

  lazy val kind: ValidationResult[String] = readTypeInfo("kind", NoKindSpecified)
  lazy val apiVersion: ValidationResult[String] = readTypeInfo("apiVersion", NoAPIVersionSpecified)
  lazy val validated: ValidatedResource = fromJson // fully validated skuber ObjectResource

  // private API

  private def readTypeInfo(fieldName: String, onMissing: ValidationFailure): ValidationResult[String] = {
    js.fields.find(field => field._1 == fieldName) match {
      case None => Either.left(onMissing)
      case Some(field) => Either.right(field._2.as[String])
    }
  }

  private def parseAs[O <: skuber.ObjectResource](implicit fmt: Format[O]): ValidationResult[JsResult[O]] = Either.right(Json.fromJson[O](js))

  /*
   * Parse the resource, returnning an instance of a concrete ObjectResource class determined by the kind and apiVersion
   */
  private def parseResource(kind: String, version: String): ValidationResult[JsResult[skuber.ObjectResource]] = {
    val typeInfo = Tuple2(kind,version)
    typeInfo match {
      case ("Service", "v1") => parseAs[skuber.Service]
      case ("Deployment", "extensions/v1beta1") => parseAs[ExtDeployment]
      case ("Deployment", "apps/v1beta1") => parseAs[AppsDeployment]
      case ("ReplicaSet", "extensions/v1beta1") => parseAs[ReplicaSet]
      case ("ReplicationController", "v1") => parseAs[skuber.ReplicationController]
      case ("StatefulSet", "apps/v1beta1") => parseAs[StatefulSet]
      case ("Ingress", "extensions/v1beta1") => parseAs[Ingress]
      case ("ConfigMap", "v1") => parseAs[skuber.ConfigMap]
      case ("Namespace", "v1") => parseAs[skuber.Namespace]
      case ("Pod", "v1") => parseAs[skuber.Pod]
      case ("ServiceAccount", "v1") => parseAs[skuber.ServiceAccount]
      case ("PersistentVolume", "v1") => parseAs[skuber.PersistentVolume]
      case ("Secret", "v1") => parseAs[skuber.Secret]
      case ("LimitRange", "v1") => parseAs[skuber.LimitRange]
      case ("DaemonSet", "extensions/v1beta1") => parseAs[DaemonSet]
      case ("Role", "rbac.authorization.k8s.io/v1beta1") => parseAs[Role]
      case ("ClusterRole", "rbac.authorization.k8s.io/v1beta1") => parseAs[ClusterRole]
      case ("RoleBinding", "rbac.authorization.k8s.io/v1beta1") => parseAs[RoleBinding]
      case ("ClusterRoleBinding", "rbac.authorization.k8s.io/v1beta1") => parseAs[ClusterRoleBinding]
      case t => Either.left(UnsupportedType(t._1, t._2))
    }
  }

  private def getResource(parseResult: ValidationResult[JsResult[skuber.ObjectResource]]): ValidatedResource = {
    parseResult.flatMap {
        case JsError(err) => Either.left(ParseError(err.toString))
        case JsSuccess(resource, _) => Either.right(resource)
    }
  }

  /*
   * Fully validated skuber model resource.
   * The kind and apiVersion are retrieved and then used to parse the resource into a specific skuber type
   * Any validation failure encountered will be returned immediately (failures are not accumulated)
   */
  private lazy val fromJson: ValidatedResource = {
    for {
      k <- kind
      v <- apiVersion
      result <- getResource(parseResource(k,v))
    } yield result
  }
}
