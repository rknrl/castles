package ru.rknrl.base.game

import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.core.rmi.Msg

class PersonalMessage(val playerId: PlayerId, val msg: Msg)
