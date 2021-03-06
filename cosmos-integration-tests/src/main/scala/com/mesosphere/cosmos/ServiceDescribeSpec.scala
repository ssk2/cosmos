package com.mesosphere.cosmos

import _root_.io.circe.Json
import _root_.io.circe.jawn._
import _root_.io.circe.syntax._
import cats.syntax.either._
import com.mesosphere.cosmos.http.HttpRequest
import com.mesosphere.cosmos.test.CosmosIntegrationTestClient.CosmosClient
import com.mesosphere.cosmos.thirdparty.marathon.model.AppId
import com.mesosphere.universe
import com.mesosphere.universe.v3.syntax.PackageDefinitionOps._
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status
import java.util.UUID
import org.scalatest.AppendedClues._
import org.scalatest.Assertion
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.prop.TableFor5

class ServiceDescribeSpec
  extends FeatureSpec
    with GivenWhenThen
    with Matchers
    with TableDrivenPropertyChecks {

  private val path: String = "service/describe"
  private val contentType: String = "application/vnd.dcos.service.describe-request+json;charset=utf-8;version=v1"
  private val accept: String = "application/vnd.dcos.service.describe-response+json;charset=utf-8;version=v1"

  feature("The service/describe endpoint") {
    scenario("The user would like to know the upgrades available to a service") {
      serviceDescribeTest { (content, _, expectedUpgrades, _, _, _) =>
        Then("the user should be able to observe the upgrades available to that service")
        val actualUpgrades = content.hcursor.get[Json]("upgradesTo")
        actualUpgrades shouldBe Right(expectedUpgrades.asJson)
      }
    }
    scenario("The user would like to know the downgrades available to a service") {
      serviceDescribeTest { (content, _, _, expectedDowngrades, _, _) =>
        Then("the user should be able to observe the downgrades available to that service")
        val actualDowngrades = content.hcursor.get[Json]("downgradesTo")
        actualDowngrades shouldBe Right(expectedDowngrades.asJson)
      }
    }
    scenario("The user would like to know the options used to run a service") {
      serviceDescribeTest { (content, _, _, _, expectedResolvedOptions, _) =>
        Then("the user should be able to observe the options used to run that service")
        val actualResolvedOptions = content.hcursor.get[Json]("resolvedOptions")
        actualResolvedOptions shouldBe Right(expectedResolvedOptions)
      }
    }
    scenario("The user would like to know the package definition used to run a service") {
      serviceDescribeTest { (content, packageDefinition, _, _, _, _) =>
        Then("the user should be able to observe the package definition used to run that service")
        val expectedDefinition = ItObjects.dropNullKeys(packageDefinition.asJson)
        val actualDefinition = content.hcursor.get[Json]("package").map(ItObjects.dropNullKeys)
        actualDefinition shouldBe Right(expectedDefinition)
      }
    }
    scenario("The user would like to know the options he provided to run a service") {
      serviceDescribeTest { (content, _, _, _, _, expectedUserProvidedOptions) =>
        Then("the user should be able to observe the options the user provided to run a service")
        val actualUserProvidedOptions = content.hcursor.get[Json]("userProvidedOptions")
        actualUserProvidedOptions shouldBe Right(expectedUserProvidedOptions)
      }
    }
  }

  private val helloWorldPackageDefinitions
  : TableFor5[universe.v4.model.PackageDefinition, List[String], List[String], Json, Json] = {
    Table(
      ("Package Definition", "Upgrades To", "Downgrades To", "Resolved Options", "User Supplied Options"),
      (ItObjects.helloWorldPackage0, List(), List(), ItObjects.helloWorldResolvedOptions(), Json.obj()),
      (ItObjects.helloWorldPackage3, List("0.4.1"), List(), ItObjects.helloWorldResolvedOptions(), Json.obj()),
      (ItObjects.helloWorldPackage4, List(), List("0.4.0"), ItObjects.helloWorldResolvedOptions(), Json.obj())
    )
  }

  private def serviceDescribeTest(
    testCode: (Json, universe.v4.model.PackageDefinition, List[String], List[String], Json, Json) => Assertion
  ): Unit = {
    forAll(helloWorldPackageDefinitions) {
      (
        packageDefinition,
        expectedUpgrades,
        expectedDowngrades,
        expectedResolvedOptions,
        expectedUserProvidedOptions
      ) =>
        Given("a running service and its appId")
        val appId = AppId(UUID.randomUUID().toString)
        val name = packageDefinition.name
        val version = packageDefinition.version.toString
        val Right(install) = ItUtil.packageInstall(name, Some(version), appId = Some(appId))

        install.appId shouldBe appId

        try {
          When("the user makes a request to the service/describe endpoint")
          val response = serviceDescribe(appId.toString)
          response.status shouldBe Status.Ok withClue response.contentString
          response.contentType shouldBe Some(accept)
          val Right(content) = parse(response.contentString)

          // the actual test
          testCode(
            content,
            packageDefinition,
            expectedUpgrades,
            expectedDowngrades,
            expectedResolvedOptions,
            expectedUserProvidedOptions
          )
          ()
        } finally {
          // clean up
          ItUtil.packageUninstall(name, appId, all = true)
          ()
        }
    }
  }

  private def serviceDescribe(appId: String): Response = {
    val body = Json.obj(
      "appId" -> appId.asJson
    )
    CosmosClient.submit(
      HttpRequest.post(
        path = path,
        body = body.noSpaces,
        contentType = Some(contentType),
        accept = Some(accept)
      )
    )
  }

}

