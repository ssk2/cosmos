package com.mesosphere.cosmos.repository

import com.mesosphere.cosmos.circe.Encoders.exceptionErrorResponse
import com.mesosphere.cosmos.storage.installqueue.ProcessorView
import com.mesosphere.cosmos.storage.v1.model.Install
import com.mesosphere.cosmos.storage.v1.model.PendingOperation
import com.mesosphere.cosmos.storage.v1.model.Uninstall
import com.mesosphere.cosmos.storage.v1.model.UniverseInstall
import com.twitter.util.Future
import com.twitter.util.Return
import com.twitter.util.Throw

final class OperationProcessor private (
  processorView: ProcessorView,
  installer: Installer,
  universeInstaller: UniverseInstaller,
  uninstaller: Uninstaller
) extends (() => Future[Unit]) {

  def apply(): Future[Unit] = {
    processorView.next().flatMap {
      case Some(pending) =>
        applyOperation(pending).transform {
          case Return(()) =>
            processorView.success(pending.packageCoordinate)
          case Throw(err) =>
            processorView.failure(
              pending.packageCoordinate,
              exceptionErrorResponse(err)
            )
        }
      case None =>
        // Nothing to do. Just return.
        Future.Done
    }
  }

  private[this] def applyOperation(pending: PendingOperation): Future[Unit] = {
    pending.operation match {
      case Install(stagedPackageId, pkg) => installer(stagedPackageId, pkg)
      case UniverseInstall(pkg) => universeInstaller(pkg)
      case Uninstall(pkg) => uninstaller(pkg)
    }
  }
}

object OperationProcessor {
  def apply(
    processorView: ProcessorView,
    installer: Installer,
    universeInstaller: UniverseInstaller,
    uninstaller: Uninstaller
  ): OperationProcessor = {
    new OperationProcessor(processorView, installer, universeInstaller, uninstaller)
  }
}
