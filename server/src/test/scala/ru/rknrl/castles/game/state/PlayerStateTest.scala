package ru.rknrl.castles.game.state

import ru.rknrl.castles.game.state.players.{PlayerId, PlayerState, PlayerStates}

object PlayerStateTest {
  def playerState(attack: Double = 1,
                  defence: Double = 2,
                  speed: Double = 3,
                  churchesPopulation: Double = 10) =
    new PlayerState(
      stat = new Stat(
        attack = attack,
        defence = defence,
        speed = speed
      ),
      churchesPopulation = churchesPopulation
    )

  val playerStates = new PlayerStates(Map(
    new PlayerId(0) → playerState(churchesPopulation = 100),
    new PlayerId(1) → playerState(churchesPopulation = 0),
    new PlayerId(2) → playerState(churchesPopulation = 20),
    new PlayerId(3) → playerState(churchesPopulation = 30)
  ))
}

class PlayerStateTest {

}
