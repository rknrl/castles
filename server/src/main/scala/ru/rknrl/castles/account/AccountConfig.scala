package ru.rknrl.castles.account

import ru.rknrl.dto.CommonDTO.BuildingLevel

class AccountConfig(val buildingPrices: Map[BuildingLevel, Int],
                    val itemPrice: Int,
                    val goldByDollar: Int)
