package ru.rknrl.castles.game

class Stat(val attack: Double,
           val defence: Double,
           val speed: Double) {
  def +(that: Stat) = new Stat(attack + that.attack, defence + that.defence, speed + that.defence)

  def *(k: Double) = new Stat(attack * k, defence * k, speed * k)
}