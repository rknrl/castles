package ru.rknrl.castles.rmi.b2c

import ru.rknrl.dto.GameDTO.GameStateDTO

abstract class EnterGameFacade {
  def joinGame(gameState: GameStateDTO) // join() answer

  def leaveGame() // leave() answer
}
