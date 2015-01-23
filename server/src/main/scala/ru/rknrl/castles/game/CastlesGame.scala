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
        if (senderCanPlay) moveActions = moveActions + (senderPlayerId → dto)

      case CastFireballMsg(point: PointDTO) ⇒
        if (senderCanPlay) fireballCasts = fireballCasts + (senderPlayerId → point)

      case CastStrengtheningMsg(buildingId: BuildingIdDTO) ⇒
        if (senderCanPlay) strengtheningCasts = strengtheningCasts + (senderPlayerId → new BuildingId(buildingId.getId))

      case CastVolcanoMsg(point: PointDTO) ⇒
        if (senderCanPlay) volcanoCasts = volcanoCasts + (senderPlayerId → point)

      case CastTornadoMsg(dto: CastTorandoDTO) ⇒
        if (senderCanPlay) tornadoCasts = tornadoCasts + (senderPlayerId → dto)

      case CastAssistanceMsg(buildingId: BuildingIdDTO) ⇒
        if (senderCanPlay) assistanceCasts = assistanceCasts + (senderPlayerId → new BuildingId(buildingId.getId))
    }
  }
}