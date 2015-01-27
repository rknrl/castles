package ru.rknrl.base.payments

import spray.http.Uri

object UriParams {
  def parseGet(uri: Uri) = new UriParams(uri.toString().split('?')(1))

  def parsePost(data: String) = new UriParams(data)
}

class UriParams private(data: String) {
  private val params = parseParams(data: String)

  def getParam(name: String) = params(name)

  def hasParam(name: String) = params.contains(name)

  private def parseParams(params: String) = params.split('&').map(splitByEqual).toMap

  private def splitByEqual(s: String) = {
    val split = s.split("=", 2)
    split(0) → split(1)
  }

  def concat = {
    val sorted = params.toList.sortBy(t ⇒ t._1)

    var s = ""
    for ((key, value) ← sorted if key != "sig") {
      s += key + "=" + value
    }
    s
  }

  override def toString = {
    var s = ""
    for ((key, value) ← params) {
      s += key + "='" + value + "'\n"
    }
    s
  }
}