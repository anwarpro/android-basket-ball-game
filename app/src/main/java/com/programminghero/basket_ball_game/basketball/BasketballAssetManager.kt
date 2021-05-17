package com.programminghero.basket_ball_game.basketball

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture

class BasketballAssetManager {
    @JvmField
    val manager: AssetManager = AssetManager(
        ResolutionFileResolver(
            InternalFileHandleResolver(),
            ResolutionFileResolver.Resolution(800, 480, "480"),
            ResolutionFileResolver.Resolution(1280, 720, "720"),
            ResolutionFileResolver.Resolution(1920, 1080, "1080")
        )
    )

    @JvmField
    val basketBall = "basketball/ball.png"

    @JvmField
    val hintCircle = "basketball/circle.png"

    @JvmField
    val hintArrowUp = "basketball/arrowup.png"

    @JvmField
    val pasue = "basketball/pause.png"

    @JvmField
    val playMore = "basketball/buttons/play-more.png"
    fun loadImages() {
        manager.load(basketBall, Texture::class.java)
        manager.load(hintCircle, Texture::class.java)
        manager.load(hintArrowUp, Texture::class.java)
        manager.load(pasue, Texture::class.java)
        manager.load(playMore, Texture::class.java)
    }

    //Sounds
    @JvmField
    val dropSound = "basketball/audio/drop.ogg"

    @JvmField
    val shootSound = "basketball/audio/shoot.ogg"

    @JvmField
    val clapSound = "basketball/audio/applause.ogg"
    fun loadSounds() {
        manager.load(dropSound, Sound::class.java)
        manager.load(shootSound, Sound::class.java)
        manager.load(clapSound, Sound::class.java)
    }

    fun dispos() {
        manager.dispose()
    }

}