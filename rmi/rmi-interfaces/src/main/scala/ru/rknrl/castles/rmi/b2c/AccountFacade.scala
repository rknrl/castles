package ru.rknrl.castles.rmi.b2c

import ru.rknrl.dto.AccountDTO.{ItemsDTO, PricesDTO, SkillsDTO, StartLocationDTO}
import ru.rknrl.dto.CommonDTO.NodeLocator

abstract class AccountFacade {
  def enteredGame(node: NodeLocator)

  def goldUpdated(value: Int)

  def pricesUpdated(prices: PricesDTO)

  def startLocationUpdated(startLocation: StartLocationDTO)

  def skillsUpdated(skills: SkillsDTO)

  def itemsUpdated(items: ItemsDTO)
}
