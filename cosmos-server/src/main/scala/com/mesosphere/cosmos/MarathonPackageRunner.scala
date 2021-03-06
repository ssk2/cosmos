package com.mesosphere.cosmos

import _root_.io.circe.JsonObject
import com.mesosphere.cosmos.circe.Decoders.decode
import com.mesosphere.cosmos.http.RequestSession
import com.mesosphere.cosmos.thirdparty.marathon.circe.Decoders._
import com.mesosphere.cosmos.thirdparty.marathon.model.{MarathonApp, MarathonError}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

/** A [[com.mesosphere.cosmos.PackageRunner]] implementation for Marathon. */
final class MarathonPackageRunner(adminRouter: AdminRouter) extends PackageRunner {

  def launch(renderedConfig: JsonObject)(implicit session: RequestSession): Future[MarathonApp] = {
    adminRouter.createApp(renderedConfig)
      .map { response =>
        response.status match {
          case Status.Conflict =>
            throw ServiceAlreadyStarted()
          case status if (400 until 500).contains(status.code) =>
            Try(decode[MarathonError](response.contentString)) match {
              case Success(marathonError) =>
                throw MarathonBadResponse(marathonError)
              case Failure(_) =>
                throw MarathonGenericError(status)
            }
          case status if (500 until 600).contains(status.code) =>
            throw MarathonBadGateway(status)
          case _ =>
            decode[MarathonApp](response.contentString)
        }
      }
  }

}
