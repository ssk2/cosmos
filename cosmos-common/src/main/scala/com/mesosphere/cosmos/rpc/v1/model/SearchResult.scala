package com.mesosphere.cosmos.rpc.v1.model

import com.mesosphere.universe

case class SearchResult(
  name: String,
  currentVersion: universe.v3.model.Version,
  versions: Map[universe.v3.model.Version, universe.v3.model.ReleaseVersion],
  description: String,
  framework: Boolean,
  tags: List[universe.v3.model.Tag],
  selected: Option[Boolean] = None,
  images: Option[universe.v3.model.Images] = None
)
