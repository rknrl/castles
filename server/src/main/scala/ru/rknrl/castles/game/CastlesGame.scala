package ru.rknrl.castles.game

import akka.actor.ActorRef
import ru.rknrl.base.game.Game
import ru.rknrl.castles.game.objects.buildings.BuildingId
import ru.rknrl.castles.game.objects.players.{Player, PlayerId}
import ru.rknrl.castles.rmi._
import ru.rknrl.dto.GameDTO._

class CastlesGame(players: Map[PlayerId, Player],
                  big: Boolean,
                  config: GameConfig,
                  matchmaking: ActorRef) extends Game(players, matchmaking, config.winReward) {

  override var gameState = GameState.init(System.currentTimeMillis(), playersList.toList, big, config)

  private var moveActions = Map[PlayerId, MoveDTO]()
  private var fireballCasts = Map[PlayerId, PointDTO]()
  private var strengtheningCasts = Map[PlayerId, BuildingId]()
  private var volcanoCasts = Map[PlayerId, PointDTO]()
  private var tornadoCasts = Map[PlayerId, CastTorandoDTO]()
  private var assistanceCasts = Map[PlayerId, BuildingId]()

  private def clearMaps(): Unit = {
    moveActions = Map.empty
    fireballCasts = Map.empty
    strengtheningCasts = Map.empty
    volcanoCasts = Map.empty
    tornadoCasts = Map.empty
    assistanceCasts = Map.empty
  }

  override def updateGameState = {
    val time = System.currentTimeMillis()

    val (newGameState, messages, personalMessages) = gameState.update(
      time,
      moveActions,
      fireballCasts,
      strengtheningCasts,
      volcanoCasts,
      tornadoCasts,
      assistanceCasts
    )

    clearMaps()

    (newGameState, messages, personalMessages)
  }

  override def receive = {
    super.receive orElse {
      case MoveMsg(dto: MoveDTO) ⇒
        assertCanPlay()
        moveActions = moveActions + (getPlayerId → dto)

      case CastFireballMsg(point: PointDTO) ⇒
        assertCanPlay()
        fireballCasts = fireballCasts + (getPlayerId → point)

      case CastStrengtheningMsg(buildingId: BuildingIdDTO) ⇒
        assertCanPlay()
        strengtheningCasts = strengtheningCasts + (getPlayerId → new BuildingId(buildingId.getId))

      case CastVolcanoMsg(point: PointDTO) ⇒
        assertCanPlay()
        volcanoCasts = volcanoCasts + (getPlayerId → point)

      case CastTornadoMsg(dto: CastTorandoDTO) ⇒
        assertCanPlay()
        tornadoCasts = tornadoCasts + (getPlayerId → dto)

      case CastAssistanceMsg(buildingId: BuildingIdDTO) ⇒
        assertCanPlay()
        assistanceCasts = assistanceCasts + (getPlayerId → new BuildingId(buildingId.getId))
    }
  }
}