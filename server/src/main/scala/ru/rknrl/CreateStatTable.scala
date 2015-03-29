//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import ru.rknrl.dto.CommonDTO.StatAction

object CreateStatTable {
  def main(args: Array[String]) {
    val builder = new StringBuilder
    builder.append("TRUNCATE stat;\n")
    for (action ← StatAction.values)
      builder.append("INSERT INTO stat (action,count) VALUES (" + action.getNumber + ",0);\n")
    println(builder.result)
  }
}
