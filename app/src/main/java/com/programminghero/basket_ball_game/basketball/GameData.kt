package com.programminghero.basket_ball_game.basketball

class GameData private constructor() {
    var isHint = false
    var isBack = false
    var isSound = false

    companion object {
        val instance = GameData()
    }
}