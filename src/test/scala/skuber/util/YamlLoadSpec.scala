package skuber.util

import java.io.File

import org.specs2.mutable.Specification

/**
  * @author David O'Riordan
  */
class YamlLoadSpec extends Specification {
  "This is a unit specification for the skuber util YAML resource loading functionality.\n ".txt

  "The guestbook Yaml can be loaded into a skuber mode" >> {

    val path = getClass.getResource("/guestbook.yaml").getPath
    val file = new File(path)
    val validatedResources = LoadResources.loadYaml(file)

    validatedResources.isRight mustEqual true
    val resources = validatedResources.right.get.toList
    resources.size mustEqual 6
    val services = resources collect { case svc: skuber.Service => svc }
    val deployments = resources collect { case depl: skuber.ext.Deployment => depl }
    services.size mustEqual 3
    deployments.size mustEqual 3
    services exists { svc => svc.name == "redis-master"} mustEqual true
    deployments exists { depl => depl.name == "redis-master"} mustEqual true
  }
}
