package com.programminghero.basket_ball_game.basketball

import aurelienribon.tweenengine.TweenAccessor
import com.badlogic.gdx.graphics.g2d.Sprite

class SpriteAccessor : TweenAccessor<Sprite> {
    override fun getValues(target: Sprite, tweenType: Int, returnValues: FloatArray): Int {
        return when (tweenType) {
            TYPE_Y -> {
                returnValues[0] = target.y
                1
            }
            TYPE_X -> {
                returnValues[0] = target.x
                1
            }
            TYPE_XY -> {
                returnValues[0] = target.x
                returnValues[1] = target.y
                2
            }
            TYPY_ALPHA -> {
                returnValues[0] = target.color.a
                1
            }
            else -> {
                assert(false)
                -1
            }
        }
    }

    override fun setValues(target: Sprite, tweenType: Int, newValues: FloatArray) {
        when (tweenType) {
            TYPE_Y -> target.y = newValues[0]
            TYPE_X -> target.x = newValues[0]
            TYPE_XY -> {
                target.x = newValues[0]
                target.y = newValues[1]
            }
            TYPY_ALPHA -> target.setColor(
                target.color.r,
                target.color.g,
                target.color.b,
                newValues[0]
            )
            else -> assert(false)
        }
    }

    companion object {
        const val TYPE_Y = 1
        const val TYPE_X = 2
        const val TYPE_XY = 3
        const val TYPY_ALPHA = 4
    }
}