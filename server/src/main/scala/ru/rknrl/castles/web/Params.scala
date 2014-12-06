package ru.rknrl.castles.web

import spray.http.Uri

class Params(uri: Uri) {
  private val params = parseParams(getParams(uri))

  def getParam(name: String) = params(name)

  def hasParam(name: String) = params.contains(name)

  private def getParams(uri: Uri) = uri.toString().split('?')(1)

  private def parseParams(params: String) = params.split('&').map(splitByEqual).toMap

  private def splitByEqual(s: String) = {
    val split = s.split("=", 2)
    split(0) → split(1)
  }
}