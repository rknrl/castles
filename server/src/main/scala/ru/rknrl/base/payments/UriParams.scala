package ru.rknrl.base.payments

import spray.http.Uri

class UriParams(uri: Uri) {
  private val params = parseParams(getParams(uri))

  def getParam(name: String) = params(name)

  def hasParam(name: String) = params.contains(name)

  private def getParams(uri: Uri) = uri.toString().split('?')(1)

  private def parseParams(params: String) = params.split('&').map(splitByEqual).toMap

  private def splitByEqual(s: String) = {
    val split = s.split("=", 2)
    split(0) → split(1)
  }

  def concat = {
    val sorted = params.toList.sorted(new Ordering[(String, String)] {
      override def compare(x: (String, String), y: (String, String)) =
        String.CASE_INSENSITIVE_ORDER.compare(x._1, y._1)
    })

    var s = ""
    for ((key, value) ← sorted) {
      s += key + "=" + value
    }
    s
  }
}