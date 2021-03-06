package com.mesosphere.cosmos.repository

import cats.syntax.either._
import com.google.common.io.CharStreams
import com.mesosphere.cosmos.rpc.v1.circe.Decoders._
import com.mesosphere.cosmos.rpc.v1.model.PackageRepository
import com.twitter.util.Try
import io.circe.jawn.decode
import java.io.InputStreamReader
import scala.util.Either
import scala.util.Left
import scala.util.Right

private[repository] class DefaultRepositories private[repository](resourceName: String) {
  private val repos: Try[Either[io.circe.Error, List[PackageRepository]]] = Try {
    Option(this.getClass.getResourceAsStream(resourceName)) match {
      case Some(is) =>
        val json = CharStreams.toString(new InputStreamReader(is))
        decode[List[PackageRepository]](json)
      case _ =>
        throw new IllegalStateException(s"Unable to load classpath resource: $resourceName")
    }
  }
}

object DefaultRepositories {
  private[this] val loaded = new DefaultRepositories("/default-repositories.json")

  def apply(): DefaultRepositories = loaded

  implicit class DefaultRepositoriesOps(val dr: DefaultRepositories) extends AnyVal {
    def get(): Try[Either[io.circe.Error, List[PackageRepository]]] = {
      dr.repos
    }

    def getOrThrow: List[PackageRepository] = {
      get().map {
        case Right(list) => list
        case Left(err) => throw err
      }.get
    }

    def getOrElse(orElse: List[PackageRepository]): List[PackageRepository] = {
      get().map(_.getOrElse(orElse)).getOrElse(orElse)
    }
  }

}
