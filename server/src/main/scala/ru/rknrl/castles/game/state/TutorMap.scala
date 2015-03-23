//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.castles.account.state.{BuildingPrototype, IJ}
import ru.rknrl.castles.game.state.buildings.Building
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}

object TutorMap {
  def buildings(buildingIdIterator: BuildingIdIterator, big: Boolean) = {
    def building(buildingType: BuildingType, buildingLevel: BuildingLevel, pos: IJ, population: Int) =
      new Building(
        buildingIdIterator.next,
        new BuildingPrototype(buildingType, buildingLevel),
        pos.toXY,
        population,
        owner = None,
        strengthened = false,
        strengtheningStartTime = 0,
        lastShootTime = 0
      )

    if (big)
      List(
        building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(4, 3), 1),
        building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(10, 3), 1),
        building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(10, 11), 1),
        building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(4, 11), 1),

        building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(2, 5), 1),
        building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(2, 9), 1),
        building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(12, 5), 1),
        building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(12, 9), 1),

        building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(6, 6), 1),
        building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(8, 6), 1),
        building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(6, 8), 1),
        building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(8, 8), 1)
      )
    else
      List(
        building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(6, 3), 1),
        building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(4, 3), 1),
        building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(0, 4), 1),

        building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(1, 7), 1),
        building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(3, 7), 1),
        building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(7, 6), 1)
      )
  }
}
