package ru.rknrl.castles.game

import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.core.rmi.Msg

class PersonalMessage(val playerId: PlayerId, val msg: Msg)
