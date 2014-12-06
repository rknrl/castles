package ru.rknrl.castles.rmi.c2b

import ru.rknrl.dto.GameDTO.{BuildingIdDTO, CastTorandoDTO, MoveDTO, PointDTO}

abstract class GameFacade {
  def move(dto: MoveDTO)

  def castFireball(point: PointDTO)

  def castStrengthening(buildingId: BuildingIdDTO)

  def castVolcano(point: PointDTO)

  def castTornado(cast: CastTorandoDTO)

  def castAssistance(buildingId: BuildingIdDTO)

  def surrender()

  def leave()
}
