package com.mesosphere.cosmos.rpc

import com.mesosphere.cosmos.http.MediaType

object MediaTypes {

  private[this] val dcos = MediaType.vndJson(List("dcos")) _
  private[this] val dcosPackage = MediaType.vndJson(List("dcos", "package")) _
  private[this] val dcosService = MediaType.vndJson(List("dcos", "service")) _
  private[this] def vnd(kind: String, version: Int = 1) = dcosPackage(kind, version)

  val UninstallRequest = vnd("uninstall-request")
  val UninstallResponse = vnd("uninstall-response")

  val ListRequest = vnd("list-request")
  val ListResponse = vnd("list-response")

  val ErrorResponse = vnd("error")
  val InstallRequest = vnd("install-request")
  val RenderRequest = vnd("render-request")
  val RenderResponse = vnd("render-response")
  val SearchRequest = vnd("search-request")
  val SearchResponse = vnd("search-response")
  val DescribeRequest = vnd("describe-request")
  val ListVersionsRequest = vnd("list-versions-request")
  val ListVersionsResponse = vnd("list-versions-response")
  val CapabilitiesResponse = dcos("capabilities", 1)

  val PackageRepositoryListRequest = vnd("repository.list-request")
  val PackageRepositoryListResponse = vnd("repository.list-response")
  val PackageRepositoryAddRequest = vnd("repository.add-request")
  val PackageRepositoryAddResponse = vnd("repository.add-response")
  val PackageRepositoryDeleteRequest = vnd("repository.delete-request")
  val PackageRepositoryDeleteResponse = vnd("repository.delete-response")

  val V1DescribeResponse = vnd("describe-response", 1)
  val V2DescribeResponse = vnd("describe-response", 2)
  val V1InstallResponse = vnd("install-response", 1)
  val V2InstallResponse = vnd("install-response", 2)
  val V1ListResponse = vnd("list-response", 1)
  val V2ListResponse = vnd("list-response", 2)

  val AddRequest = vnd("add-request")
  val AddResponse = vnd("add-response")

  val ServiceStartRequest = dcosService("start-request", 1)
  val ServiceStartResponse = dcosService("start-response", 1)

  val ServiceDescribeRequest = dcosService("describe-request", 1)
  val ServiceDescribeResponse = dcosService("describe-response", 1)
}
