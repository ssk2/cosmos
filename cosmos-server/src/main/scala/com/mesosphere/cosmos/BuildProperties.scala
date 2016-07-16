package com.mesosphere.cosmos

import java.util.Properties

private[cosmos] class BuildProperties private[cosmos](resourceName: String) {
  private val props = {
    val props = new Properties()
    val is = this.getClass.getResourceAsStream(resourceName)
    if (is == null) {
      throw new IllegalStateException(s"Unable to load classpath resources: $resourceName")
    }
    props.load(is)
    is.close()

    props
  }
}

object BuildProperties {
  private[this] val loaded = new BuildProperties("/build.properties")

  def apply() = loaded

  implicit class BuildPropertiesOps(val bp: BuildProperties) {

    val cosmosVersion: String = Option(bp.props.getProperty("cosmos.version")).getOrElse("unknown-version")

  }

}
