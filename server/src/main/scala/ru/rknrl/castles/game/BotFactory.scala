//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import akka.actor.{ActorContext, ActorRef, Props}
import ru.rknrl.castles.bot.{Bot, TutorBot}
import ru.rknrl.dto.AccountId

trait IBotFactory {
  def create(accountId: AccountId, isTutor: Boolean, bugs: ActorRef)
            (implicit context: ActorContext): ActorRef
}

class BotFactory extends IBotFactory {
  def create(accountId: AccountId, isTutor: Boolean, bugs: ActorRef)
            (implicit context: ActorContext) = {
    val botClass = if (isTutor) classOf[TutorBot] else classOf[Bot]
    context.actorOf(Props(botClass, accountId, bugs), accountId.id)
  }
}

class FakeBotFactory(ref: ActorRef) extends IBotFactory {
  def create(accountId: AccountId, isTutor: Boolean, bugs: ActorRef)
            (implicit context: ActorContext) =
    ref
}