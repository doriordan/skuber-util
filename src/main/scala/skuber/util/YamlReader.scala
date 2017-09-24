package skuber.util

import java.io.InputStreamReader
import java.util

import org.yaml.snakeyaml.constructor.SafeConstructor
import play.api.libs.json.{JsObject, Json}

/**
  * @author David O'Riordan
  *
  * This supports reading of resources represented as Yaml into one or more Json objects.
  */
object YamlReader {
  /*
   * Convert YAML string to JSON string representation
   */
  private def yamlStringToJsonString(yamlStr: String) = {
    import com.fasterxml.jackson.databind.ObjectMapper
    import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

    val yamlReader = new ObjectMapper(new YAMLFactory)
    val obj = yamlReader.readValue(yamlStr, classOf[Object])
    val jsonWriter = new ObjectMapper()
    jsonWriter.writeValueAsString(obj)
  }

  def read(is: InputStreamReader): Seq[JsObject] = {
    import org.yaml.snakeyaml.Yaml
    import org.yaml.snakeyaml.constructor.SafeConstructor
    import scala.collection.JavaConverters._

    val yaml = new Yaml(new SafeConstructor)
    val yamlDocs = yaml.loadAll(is).iterator.asScala
    yamlDocs.map { yamlDoc =>
      val jsonString = yamlStringToJsonString(yamlDoc.toString)
      Json.parse(jsonString).as[JsObject]
    }.toSeq
  }
}
