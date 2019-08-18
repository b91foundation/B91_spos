package com.wavesplatform.settings

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

class RestAPISettingsSpecification extends FlatSpec with Matchers {
  "RestAPISettings" should "read values" in {
    val config = ConfigFactory.parseString(
      """
        |vsys {
        |  rest-api {
        |    enable: yes
        |    bind-address: "127.0.0.1"
        |    port: 8922
        |    api-key-hash: "BASE58APIKEYHASH"
        |    cors: yes
        |  }
        |}
      """.stripMargin)
    val settings = RestAPISettings.fromConfig(config)

    settings.enable should be(true)
    settings.bindAddress should be("127.0.0.1")
    settings.port should be(8922)
    settings.apiKeyHash should be ("BASE58APIKEYHASH")
    settings.cors should be(true)
  }

}
