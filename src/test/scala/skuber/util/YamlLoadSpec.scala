package skuber.util

import java.io.File

import org.specs2.mutable.Specification
import skuber.{ObjectResource, Service}
import skuber.ext.Deployment

/**
  * @author David O'Riordan
  */
class YamlLoadSpec extends Specification {
  "This is a unit specification for the skuber util YAML resource loading functionality.\n ".txt

  "The guestbook Yaml can be loaded into a skuber model" >> {

    val path = getClass.getResource("/guestbook.yaml").getPath
    val file = new File(path)
    val validatedResources = LoadResources.loadYaml(file)

    validatedResources.isRight mustEqual true
    val resources = validatedResources.right.get.toList
    resources must haveSize(6)
    val services = resources collect { case svc: skuber.Service => svc }
    val deployments = resources collect { case depl: skuber.ext.Deployment => depl }
    services must haveSize(3)
    deployments must haveSize(3)
    val redisMasterDeploymentOpt=YamlLoadSpec.find[Deployment](resources, "redis-master")
    redisMasterDeploymentOpt must beSome
    val redisMasterServiceOpt=YamlLoadSpec.find[Service](resources, "redis-master")
    redisMasterServiceOpt must beSome
    val redisSlaveDeploymentOpt=YamlLoadSpec.find[Deployment](resources, "redis-slave")
    redisSlaveDeploymentOpt must beSome
    val redisSlaveServiceOpt=YamlLoadSpec.find[Service](resources, "redis-slave")
    redisSlaveServiceOpt must beSome
    val frontendDeploymentOpt=YamlLoadSpec.find[Deployment](resources, "frontend")
    frontendDeploymentOpt must beSome
    val frontendServiceOpt=YamlLoadSpec.find[Service](resources, "frontend")
    frontendServiceOpt must beSome
  }
}

object YamlLoadSpec {
  import scala.reflect.ClassTag
  def find[O <: ObjectResource](resources: List[ObjectResource], resourceName: String)(implicit tag: ClassTag[O]) : Option[O] = {
    resources.collectFirst[O] {
      case resource: O if resource.name == resourceName => resource
    }
  }
}
