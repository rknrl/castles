//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import java.io._

import scala.io.Source

object AddHeaders {
  val header = """//       ___       ___       ___       ___       ___
                 |//      /\  \     /\__\     /\__\     /\  \     /\__\
                 |//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
                 |//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
                 |//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
                 |//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
                 |//      \|__|     \|__|     \/__/     \|__|     \/__/
                 |
                 | """.stripMargin

  val headerFirstLine = header.takeWhile(_ != '\n')

  def main(args: Array[String]) = {
    val dir = "/Users/tolyayanot/dev/rknrl/core/"
    val files = recursiveListFiles(new File(dir))
    for (f ← files) {
      println(f.getAbsolutePath)
      val text = Source.fromFile(f.getAbsolutePath).mkString
      if (text.takeWhile(_ != '\n') != headerFirstLine) {
        println("add header")
        val out = new PrintWriter(f, "UTF-8")
        try {
          out.print(header + text)
        } finally {
          out.close()
        }
      }
    }
  }

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles(new FileFilter {
      override def accept(pathname: File) =
        pathname.getName.endsWith(".as") || pathname.getName.endsWith(".scala")
    })
    these ++ f.listFiles.filter(_.isDirectory).flatMap(recursiveListFiles)
  }
}
