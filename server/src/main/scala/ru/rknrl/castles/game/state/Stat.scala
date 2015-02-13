//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

class Stat(val attack: Double,
           val defence: Double,
           val speed: Double) {
  def +(that: Stat) = new Stat(attack + that.attack, defence + that.defence, speed + that.speed)

  def *(that: Stat) = new Stat(attack * that.attack, defence * that.defence, speed * that.speed)
}
