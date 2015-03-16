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

  def buildings(buildingIdIterator: BuildingIdIterator) = {

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

    List(
      building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(4, 4), 5),
      building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(10, 4), 5),
      building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(10, 10), 5),
      building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, new IJ(4, 10), 5),

      building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(7, 2), 4),
      building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(7, 12), 4),
      building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(2, 7), 4),
      building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, new IJ(12, 7), 4),

      building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(6, 6), 3),
      building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(8, 6), 3),
      building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(8, 8), 3),
      building(BuildingType.TOWER, BuildingLevel.LEVEL_2, new IJ(6, 8), 3)
    )
  }
}
