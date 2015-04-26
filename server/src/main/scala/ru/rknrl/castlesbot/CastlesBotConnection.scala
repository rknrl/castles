//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castlesbot

import akka.actor.{ActorRef, Props}
import ru.rknrl.dto.AccountId
import ru.rknrl.rmi.Server

class CastlesBotConnection(host: String, port: Int, accountId: AccountId, bugs: ActorRef) extends Server(host, port) {

  val handler = context.actorOf(Props(classOf[CastlesBot], self, accountId, bugs))

}