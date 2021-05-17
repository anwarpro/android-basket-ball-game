package com.programminghero.basket_ball_game.basketball

import aurelienribon.tweenengine.TweenAccessor
import com.badlogic.gdx.physics.box2d.Shape

class ShapeAccessor : TweenAccessor<Shape> {
    override fun getValues(target: Shape, tweenType: Int, returnValues: FloatArray): Int {
        return when (tweenType) {
            TYPE_RADIUS -> {
                returnValues[0] = target.radius
                1
            }
            else -> {
                assert(false)
                -1
            }
        }
    }

    override fun setValues(target: Shape, tweenType: Int, newValues: FloatArray) {
        when (tweenType) {
            TYPE_RADIUS -> target.radius = newValues[0]
            else -> assert(false)
        }
    }

    companion object {
        const val TYPE_RADIUS = 1
    }
}