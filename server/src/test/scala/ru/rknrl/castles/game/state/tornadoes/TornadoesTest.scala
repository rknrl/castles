package ru.rknrl.castles.game.state.tornadoes

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.game.state.PlayerStateTest
import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.castles.mock.GameConfigMock
import ru.rknrl.dto.GameDTO.{CastTorandoDTO, PointDTO}
import ru.rknrl.utils.{Point, Points}

class TornadoesTest extends FlatSpec with Matchers {
  private val points = new Points(Vector(new Point(0, 0), new Point(1, 1)))
  private val tornado0 = new Tornado(new PlayerId(0), points, startTime = 800, duration = 2000, speed = 0.004)
  private val tornado1 = new Tornado(new PlayerId(1), points, startTime = 1000, duration = 500, speed = 0.004)
  private val tornado2 = new Tornado(new PlayerId(2), points, startTime = 900, duration = 2000, speed = 0.004)

  private val config = GameConfigMock.gameConfig()

  "cast->tornadoes" should "work with empty map" in {
    Tornadoes.`casts→tornadoes`(Map.empty, 423, config, PlayerStateTest.playerStates).size should be(0)
  }

  import scala.collection.JavaConverters._

  private val point1 = PointDTO.newBuilder().setX(0.44f).setY(0.55f).build
  private val point2 = PointDTO.newBuilder().setX(0.55f).setY(0.66f).build
  private val point3 = PointDTO.newBuilder().setX(0.66f).setY(0.77f).build
  private val dtoPoints = List(point1, point2, point3).asJava

  "cast->tornadoes" should "return tornadoes" in {
    val tornado0 = new PlayerId(0) → CastTorandoDTO.newBuilder().addAllPoints(dtoPoints).build
    val tornado1 = new PlayerId(1) → CastTorandoDTO.newBuilder().addAllPoints(dtoPoints).build
    val tornado2 = new PlayerId(2) → CastTorandoDTO.newBuilder().addAllPoints(dtoPoints).build

    val tornadoes = Tornadoes.`casts→tornadoes`(Map(tornado0, tornado1, tornado2), 123, config, PlayerStateTest.playerStates).toList
    tornadoes.size should be(3)

    tornadoes(0).playerId should be(new PlayerId(0))
    tornadoes(1).playerId should be(new PlayerId(1))
    tornadoes(2).playerId should be(new PlayerId(2))
  }

  "tornadoes->addMessages" should "work with empty list" in {
    Tornadoes.`tornadoes→addMessages`(List.empty, 423).size should be(0)
  }

  "tornadoes->addMessages" should "return messages" in {
    val messages = Tornadoes.`tornadoes→addMessages`(List(tornado0, tornado1, tornado2), 1200).toList
    messages.size should be(3)

    messages(0).tornadoDTO.getMillisFromStart should be(400)
    messages(1).tornadoDTO.getMillisFromStart should be(200)
    messages(2).tornadoDTO.getMillisFromStart should be(300)
  }
}

