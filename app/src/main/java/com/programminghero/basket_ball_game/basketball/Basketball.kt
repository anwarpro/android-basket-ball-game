package com.programminghero.basket_ball_game.basketball

import android.content.Context
import aurelienribon.tweenengine.*
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureListener
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.*
import com.programminghero.basket_ball_game.basketball.GameData.Companion.instance
import java.util.*

class Basketball(cn: Context?) : ApplicationAdapter(), GestureListener, ContactListener {
    val VIRTUAL_HEIGHT = 8f
    private val manager: TweenManager
    private val data: GameData
    var cam: OrthographicCamera? = null
    var batch: SpriteBatch? = null
    var fileResolver // +++
            : ResolutionFileResolver? = null
    var gravity = -9.81f // earths gravity is around 9.81 m/s^2 downwards
    private lateinit var world: World

    //    private Box2DDebugRenderer debugRender;
    private lateinit var ballBody: Body
    private lateinit var leftBody: Body
    private lateinit var rightBody: Body

    private var groundFix: Fixture? = null
    private var groundFixTop: Fixture? = null
    private var groundBody: Body? = null
    private var topOfBasket = false
    private var point: Vector3? = null
    private var wasTouched = false
    private var point2: Vector3? = null
    private var shoot = false
    private var win = false
    private var dropSound1: Sound? = null
    private var shootSound: Sound? = null
    private var croowedSound: Sound? = null
    private var spriteBall: Sprite? = null
    private var spriteFloor: Sprite? = null
    private var spriteWall: Sprite? = null
    private var spriteTopMonitor: Sprite? = null
    private var spriteSideMonitor: Sprite? = null
    private var spriteBasketRim: Sprite? = null
    private var spriteBasketBack: Sprite? = null
    private var ballRemain = 3
    private var ballStored = 0
    private var round = 1
    private var score = 0
    private var basketSensor: Body? = null
    private lateinit var spriteBallJar: Array<Sprite?>
    private var font70: BitmapFont? = null
    private var uiCam: OrthographicCamera? = null
    private var currentJar: Sprite? = null
    private var currentContainer: Sprite? = null

    private lateinit var winEmoji: Array<Sprite?>
    private var drawEmoji = false
    private var emoJiAnimatino: Tween? = null
    private lateinit var spriteBallContainer: Array<Sprite?>

    private var spriteGem: Sprite? = null
    private var drawGem = false
    private var gemAnimation: Tween? = null
    private var leftLine: Body? = null
    private var rightLine: Body? = null
    private var gameOver = false
    private var xpos = 0f
    private var retry = 0
    private var leftInJar: Tween? = null
    private var topDownContainer: Tween? = null
    private var number = 0
    private var spriteHintCircle: Sprite? = null
    private var spriteHintArrow: Sprite? = null
    private var spriteGameOver: Sprite? = null
    private var moving = false
    private var flashHintAnim: Tween? = null
    private var flashHintAnimUp: Tween? = null
    private var spritePlayMore: Sprite? = null
    private var spriteNextLesson: Sprite? = null
    private var spriteReplay: Sprite? = null
    private var skipOption = false
    private var ballShoot = 0
    private var skipOption2 = false
    private var gem = 0
    private var spriteNextLesson2: Sprite? = null
    private lateinit var gemBody: Body
    private lateinit var assMan: BasketballAssetManager
    private fun makeChainShape(world: World, body: Body?, increment: Boolean): Body {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.StaticBody
        bodyDef.angle = 0f
        val bodyCreate = world.createBody(bodyDef)
        val es = EdgeShape()
        es[if (increment) body!!.position.x + 0.1f else body!!.position.x - 0.1f, body.position.y - 0.8f, body.position.x] =
            body.position.y - 0.1f
        val fixtureDef = makeFixture(STEEL, es)
        fixtureDef.isSensor = true
        bodyCreate.createFixture(fixtureDef)
        es.dispose()
        return bodyCreate
    }

    private fun createGemCircle() {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.StaticBody
        bodyDef.position[leftBody!!.position.x + (rightBody!!.position.x - leftBody!!.position.x) / 2f] =
            leftBody!!.position.y - 1.5f
        gemBody = world.createBody(bodyDef)

        // Create a circle shape and set its radius to 6
        val circle = CircleShape()
        circle.radius = 0.25f

        // Create a fixture definition to apply our shape to
        val fixtureDef = FixtureDef()
        fixtureDef.shape = circle
        fixtureDef.density = 0f
        fixtureDef.isSensor = true
        gemBody.createFixture(fixtureDef)
        gemBody.userData = "GEM"
        circle.dispose()
    }

    private fun createBall() {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position[xpos] = 3f
        ballBody = world.createBody(bodyDef)

        // Create a circle shape and set its radius to 6
        val circle = CircleShape()
        circle.radius = BALL_RADIOS

        // Create a fixture definition to apply our shape to
        val fixtureDef = FixtureDef()
        fixtureDef.shape = circle
        fixtureDef.density = 0.000001f
        fixtureDef.friction = 0.5f
        fixtureDef.restitution = 0.5f // Make it bounce a little bit
        ballBody.createFixture(fixtureDef)
        circle.dispose()
        shoot = false
    }

    /**
     * Floor Section Of the Game
     */
    private fun createFloor() {
        // First we create a body definition
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.StaticBody
        bodyDef.position[cam!!.viewportWidth / 2f - BALL_RADIOS] = 6f
        leftBody = world!!.createBody(bodyDef)
        val circle = CircleShape()
        val fixtureDef = FixtureDef()
        circle.radius = RIM_RADIOS
        fixtureDef.shape = circle
        fixtureDef.density = 0f
        fixtureDef.friction = 1f
        fixtureDef.restitution = 0.2f // Make it bounce a little bit
        fixtureDef.isSensor = true
        leftBody.createFixture(fixtureDef)
        bodyDef.position[leftBody.position.x + 2 * BALL_RADIOS] =
            leftBody.position.y
        rightBody = world.createBody(bodyDef)
        rightBody.createFixture(fixtureDef)
        leftBody.userData = "beep"
        rightBody.userData = "beep"
        circle.dispose()

        // Create our body definition
        val groundBodyDef = BodyDef()
        groundBodyDef.type = BodyDef.BodyType.KinematicBody
        groundBodyDef.position.set(Vector2(0f, 0f))
        groundBody = world.createBody(groundBodyDef)
        groundBody?.userData = "beep"
        val groundBox = PolygonShape()
        groundBox.setAsBox(cam!!.viewportWidth, 0.4f)
        groundFix = groundBody?.createFixture(makeFixture(WOOD, groundBox))
        val groundBodyTop = world.createBody(groundBodyDef)
        groundBox.setAsBox(cam!!.viewportWidth, UPPER_GROUND_Y)
        val fixtureDef1 = FixtureDef()
        fixtureDef1.density = 0f
        fixtureDef1.isSensor = true
        fixtureDef1.shape = groundBox
        groundFixTop = groundBodyTop.createFixture(fixtureDef1)
        groundBodyTop.userData = "beep"

        //create basket sensor
        bodyDef.type = BodyDef.BodyType.StaticBody
        bodyDef.position[leftBody.getPosition().x + BALL_RADIOS + 0.008f] =
            leftBody.getPosition().y - 0.3f
        basketSensor = world!!.createBody(bodyDef)
        groundBox.setAsBox(
            BALL_RADIOS - 0.06f,
            0.05f
        )
        fixtureDef1.shape = groundBox
        basketSensor?.createFixture(fixtureDef1)
        basketSensor?.userData = "BASKET"
        groundBox.dispose()

        // Create our body definition
        val basketSensorBodyDef = BodyDef()
        basketSensorBodyDef.type = BodyDef.BodyType.StaticBody
        basketSensorBodyDef.position.set(Vector2(0f, leftBody.position.y))
        val sensor = world.createBody(basketSensorBodyDef)
        sensor.userData = "basketline"
        val groundBoxS = PolygonShape()
        groundBoxS.setAsBox(cam!!.viewportWidth, 0.01f)
        val fixtureDef2 = FixtureDef()
        fixtureDef2.density = 0f
        fixtureDef2.isSensor = true
        fixtureDef2.shape = groundBoxS
        sensor.createFixture(fixtureDef2)
        groundBoxS.dispose()
    }

    /**
     * Load Game Asset
     * texture,spriteBall - > Basketball
     *
     *
     * loadAsset is CallFrom -> create()
     */
    private fun loadAsset() {
//        texture = new Texture(fileResolver.resolve("basketball/ball.png")); // +++
        spriteBall = Sprite(assMan!!.manager.get(assMan!!.basketBall, Texture::class.java))
        spriteHintCircle = Sprite(assMan!!.manager.get(assMan!!.hintCircle, Texture::class.java))
        spriteHintArrow = Sprite(assMan!!.manager.get(assMan!!.hintArrowUp, Texture::class.java))
        spriteGameOver = Sprite(assMan!!.manager.get(assMan!!.pasue, Texture::class.java))
        spritePlayMore = Sprite(assMan!!.manager.get(assMan!!.playMore, Texture::class.java))
        spriteNextLesson =
            Sprite(Texture(fileResolver!!.resolve("basketball/buttons/next-lesson.png")))
        spriteReplay = Sprite(Texture(fileResolver!!.resolve("basketball/buttons/Replay.png")))
        spriteNextLesson2 =
            Sprite(Texture(fileResolver!!.resolve("basketball/buttons/next-lesson.png")))
        spriteBasketRim =
            Sprite(Texture(fileResolver!!.resolve("basketball/new/busket-transprans.png")))
        spriteFloor = Sprite(Texture(fileResolver!!.resolve("basketball/new/flor.png")))
        spriteWall = Sprite(Texture(fileResolver!!.resolve("basketball/new/wall.png")))
        spriteTopMonitor = Sprite(Texture(fileResolver!!.resolve("basketball/new/monitor.png")))
        spriteSideMonitor = Sprite(Texture(fileResolver!!.resolve("basketball/new/monitor2.png")))
        spriteBasketBack =
            Sprite(Texture(fileResolver!!.resolve("basketball/new/basket-background.png")))
        spriteGem = Sprite(Texture(fileResolver!!.resolve("basketball/gem.png")))

        spriteBallJar = arrayOfNulls(4)

        for (i in 0..3) {
            spriteBallJar[i] =
                Sprite(Texture(fileResolver!!.resolve("basketball/basketjar/$i.png")))
            spriteBallJar[i]!!.y = 3.4f
            if (i == 0 || i == 1) {
                spriteBallJar[i]!!.x = -0.2f
            } else {
                spriteBallJar[i]!!.x = 0.2f
            }
        }
        spriteBallContainer = arrayOfNulls(4)
        for (i in 0..3) {
            spriteBallContainer[i] = Sprite(
                Texture(
                    fileResolver!!.resolve(
                        "basketball/basketjar/$i.png"
                    )
                )
            )
            spriteBallContainer[i]!!.y = 1f + BALL_RADIOS
            spriteBallContainer[i]!!.x = 0.1f
        }
        winEmoji = arrayOfNulls(5)
        for (i in 0..4) {
            winEmoji[i] = Sprite(
                Texture(
                    fileResolver!!.resolve("basketball/emoji/win$i.png")
                )
            )
        }
        val generator =
            FreeTypeFontGenerator(Gdx.files.internal("basketball/font/Roboto-Black.ttf"))
        val parameter = FreeTypeFontParameter()
        parameter.size = 60
        font70 = generator.generateFont(parameter) // font size 12 pixels
        generator.dispose() // don't forget to dispose to avoid memory leaks!
    }

    override fun create() {
        assMan = BasketballAssetManager()
        fileResolver = ResolutionFileResolver(
            InternalFileHandleResolver(),
            ResolutionFileResolver.Resolution(800, 480, "480"),
            ResolutionFileResolver.Resolution(1280, 720, "720"),
            ResolutionFileResolver.Resolution(1920, 1080, "1080")
        )
        batch = SpriteBatch()
        assMan!!.loadImages()
        assMan!!.loadSounds()
        assMan!!.manager.finishLoading()
        loadAsset()
        cam = OrthographicCamera()
        cam!!.setToOrtho(
            false, VIRTUAL_HEIGHT * Gdx.graphics.width / Gdx.graphics.height
                .toFloat(), VIRTUAL_HEIGHT
        ) // +++
        xpos = cam!!.viewportWidth / 2f
        uiCam = OrthographicCamera()
        uiCam!!.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        uiCam!!.position[uiCam!!.viewportWidth / 2f, uiCam!!.viewportHeight / 2f] = 0f
        world = World(Vector2(0f, gravity), true)
        //        debugRender = new Box2DDebugRenderer();
        createBall()
        createFloor()
        createGemCircle()
        leftLine = makeChainShape(world, leftBody, true)
        rightLine = makeChainShape(world, rightBody, false)
        soundLoad()
        Gdx.input.inputProcessor = GestureDetector(this)
        world.setContactListener(this)
        gameOver = false
        round = 1
        score = 0
        if (!data.isHint) {
            spriteHintArrow!!.y = cam!!.viewportHeight / 2f - 1f
            flashHintAnimUp = Tween.from(spriteHintArrow, SpriteAccessor.TYPE_Y, 1f)
                .target(2f)
                .ease(TweenEquations.easeNone)
                .repeat(Tween.INFINITY, 1f)
                .start(manager)
            flashHintAnim = Tween.from(spriteHintCircle, SpriteAccessor.TYPY_ALPHA, 2f)
                .target(0f)
                .ease(TweenEquations.easeNone)
                .repeat(Tween.INFINITY, 1f)
                .start(manager)
        }
    }

    private fun soundLoad() {
        dropSound1 = assMan!!.manager.get(assMan!!.dropSound, Sound::class.java)
        shootSound = assMan!!.manager.get(assMan!!.shootSound, Sound::class.java)
        croowedSound = assMan!!.manager.get(assMan!!.clapSound, Sound::class.java)
    }

    private fun showSkipOption() {
        batch!!.projectionMatrix = uiCam!!.combined

        //out of other two buttons
        spriteReplay!!.setSize(1f, 1f)
        spriteReplay!!.setOriginCenter()
        spriteReplay!!.setPosition(-100f, -100f)
        spriteNextLesson2!!.setSize(1f, 1f)
        spriteNextLesson2!!.setOriginCenter()
        spriteNextLesson2!!.setPosition(-100f, -100f)
        batch!!.draw(spriteGameOver, 0f, 0f, uiCam!!.viewportWidth, uiCam!!.viewportHeight)
        spritePlayMore!!.setSize(279f, 78f)
        spritePlayMore!!.setOriginCenter()
        spritePlayMore!!.setPosition(
            uiCam!!.viewportWidth / 2f - spritePlayMore!!.width / 2f,
            uiCam!!.viewportHeight / 2f + 100 - spritePlayMore!!.height / 2f
        )
        spriteNextLesson!!.setSize(279f, 78f)
        spriteNextLesson!!.setOriginCenter()
        spriteNextLesson!!.setPosition(
            uiCam!!.viewportWidth / 2f - spriteNextLesson!!.width / 2f,
            uiCam!!.viewportHeight / 2f - 100 - spriteNextLesson!!.height / 2f
        )
        spritePlayMore!!.draw(batch)
        spriteNextLesson!!.draw(batch)
        batch!!.projectionMatrix = cam!!.combined
    }

    override fun resize(width: Int, height: Int) {
        cam!!.setToOrtho(false, VIRTUAL_HEIGHT * width / height.toFloat(), VIRTUAL_HEIGHT) // +++
        batch!!.projectionMatrix = cam!!.combined
        cam!!.update()
    }

    override fun render() {
        if (assMan!!.manager.update()) {
            cam!!.update()
            manager.update(Gdx.graphics.deltaTime)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            val r = ballBody!!.fixtureList[0].shape.radius
            batch!!.begin()
            batch!!.draw(spriteFloor, 0f, 0f, cam!!.viewportWidth, UPPER_GROUND_Y + 0.5f)
            batch!!.draw(
                spriteWall, 0f, UPPER_GROUND_Y + 0.5f,
                cam!!.viewportWidth, 8 - (UPPER_GROUND_Y + 0.5f)
            )
            drawTopMonitor()
            drawScore()
            if (drawEmoji) {
                batch!!.draw(
                    winEmoji[number], winEmoji[number]!!.x,
                    spriteTopMonitor!!.y + spriteTopMonitor!!.height / 4f, 0.5f, 0.5f
                )
            }
            spriteBasketBack!!.setSize(
                rightBody!!.position.x - leftBody!!.position.x + 2 * 0.5f,
                1.5f
            )
            spriteBasketBack!!.setOriginCenter()
            batch!!.draw(
                spriteBasketBack, leftBody!!.position.x - 0.5f, 5f,
                rightBody!!.position.x - leftBody!!.position.x + 2 * 0.5f, 1.5f
            )
            if (!topOfBasket) {
                spriteBasketRim!!.setSize(
                    rightBody!!.position.x - leftBody!!.position.x,
                    2 * BALL_RADIOS
                )
                spriteBasketRim!!.setOriginCenter()
                batch!!.draw(
                    spriteBasketRim,
                    leftBody!!.position.x,
                    leftBody!!.position.y + 3 * RIM_RADIOS - 2 * BALL_RADIOS,
                    rightBody!!.position.x - leftBody!!.position.x,
                    2 * BALL_RADIOS
                )
            }
            if (shoot && ballBody!!.linearVelocity.y == 0f ||
                (ballBody!!.position.x + BALL_RADIOS < 0
                        || ballBody!!.position.x - BALL_RADIOS > cam!!.viewportWidth)
            ) {
                if (win) {
                    ballStored++
                    win = false
                } else {
                    //lose
                }

                //first round check
                if (round == 1 && ballStored < 3) {
                    if (ballShoot < 6) {
                        resetGame()
                    } else {
                        skipOption = true
                    }
                } else if (round == 1 && ballStored >= 3) { //check
                    ballRemain = ballStored
                    ballStored = 0
                    round++
                    leftInJar = null
                    moving = true
                    topDownContainer = null

                    //reset game after animation done();
                } else if (round == 2) {
                    if (ballRemain > 0) {
                        resetGame()
                    } else {
                        if (ballStored > 0) {
                            ballRemain = ballStored
                            ballStored = 0
                            round++
                            leftInJar = null
                            moving = true
                            skipOption2 = true
                            topDownContainer = null
                            val left = 0.5f + BALL_RADIOS
                            val right = cam!!.viewportWidth - 0.5f - BALL_RADIOS
                            xpos = if (round % 2 == 0) {
                                left
                            } else {
                                right
                            }

                            //reset game after animation done();
                        } else {
                            //game over
                            gameOver = true
                        }
                    }
                } else {
                    if (ballRemain > 0) {
                        resetGame()
                    } else {
                        if (ballStored > 0) {
                            ballRemain = ballStored
                            ballStored = 0
                            round++
                            if (round > 5) {
                                skipOption2 = true
                            }
                            leftInJar = null
                            moving = true
                            topDownContainer = null
                            val left = 0.5f + BALL_RADIOS
                            val right = cam!!.viewportWidth - 0.5f - BALL_RADIOS
                            if (round > 2) {
                                xpos = if (round % 2 == 0) {
                                    left
                                } else {
                                    right
                                }
                            }

                            //reset game after animation done
                        } else {
                            //game over
                            gameOver = true
                        }
                    }
                }
            }
            if (round > 1) {
                if (ballStored > 0) {
                    currentJar = if (ballStored >= 3) {
                        spriteBallJar[3]
                    } else {
                        spriteBallJar[ballStored]
                    }
                } else {
                    currentJar = spriteBallJar[0]
                    if (leftInJar == null && !moving) {
                        leftInAnimation()
                    }
                }
            } else {
                if (ballStored > 0) {
                    if (ballStored >= 3) {
                        currentJar = spriteBallJar[3]
                    } else {
                        currentJar = spriteBallJar[ballStored]
                        if (leftInJar == null && !moving) {
                            leftInAnimation()
                        }
                    }
                }
            }
            if (currentJar != null && !moving) {
                batch!!.draw(currentJar, currentJar!!.x, currentJar!!.y, 0.5f, 1f)
            }
            if (ballRemain >= 0 && ballRemain < 4) {
                currentContainer = spriteBallContainer[ballRemain]
                if (moving && topDownContainer == null) {
                    topDownAnimation()
                }
            } else {
                currentContainer = spriteBallContainer[0]
                if (moving && topDownContainer == null) {
                    topDownAnimation()
                }
            }
            if (currentContainer != null && round > 1) {
                batch!!.draw(currentContainer, currentContainer!!.x, currentContainer!!.y, 0.5f, 1f)
            }
            if (round == 2 && !data.isBack && !topOfBasket) {
                drawGemSprite()
            }
            if (!moving) {
                drawBall(r)
            }
            if (!data.isHint) {
                drawHints(r)
            }
            if (topOfBasket) {
                spriteBasketRim!!.setOriginCenter()
                batch!!.draw(
                    spriteBasketRim,
                    leftBody!!.position.x,
                    leftBody!!.position.y + 3 * RIM_RADIOS - 2 * BALL_RADIOS,
                    rightBody!!.position.x - leftBody!!.position.x,
                    2 * BALL_RADIOS
                )
            }
            if (round == 2 && !data.isBack && topOfBasket) {
                drawGemSprite()
            }
            if (gameOver) {
                gameOver()
            }
            if (skipOption) {
                showSkipOption()
            }
            if (skipOption2) {
                showSkipOption()
            }
            batch!!.end()

//        debugRender.render(world, cam.combined);
            world!!.step(1 / 60f, 6, 2)
        } else {
        }
    }

    private fun topDownAnimation() {
        topDownContainer = Tween.from(currentContainer, SpriteAccessor.TYPE_Y, 1f)
            .target(3.4f)
            .ease(TweenEquations.easeNone)
        Timeline.createSequence()
            .push(topDownContainer)
            .pushPause(0.8f)
            .setCallback { type: Int, source: BaseTween<*>? ->
                leftInJar = null
                if (!skipOption2) {
                    ballRemain--
                    resetGame()
                }
                moving = false
            }
            .setCallbackTriggers(TweenCallback.COMPLETE)
            .start(manager)
    }

    private fun leftInAnimation() {
        if (currentJar!!.x == 0.2f) {
            currentJar!!.x = -0.2f
        }
        leftInJar = Tween.to(currentJar, SpriteAccessor.TYPE_X, 1f)
            .target(0.2f)
            .ease(TweenEquations.easeNone)
            .start(manager)
    }

    private fun drawTopMonitor() {
        spriteTopMonitor!!.setSize(cam!!.viewportWidth - 2 * 0.5f, 0.8f)
        spriteTopMonitor!!.setPosition(0.5f, 8 - 1.3f)
        batch!!.draw(
            spriteTopMonitor, spriteTopMonitor!!.x, spriteTopMonitor!!.y,
            spriteTopMonitor!!.width, spriteTopMonitor!!.height
        )
    }

    private fun drawGemSprite() {
        val gemR = gemBody!!.fixtureList[0].shape.radius
        if (drawGem) {
            spriteGem!!.setSize(2 * gemR, 2 * gemR)
            spriteGem!!.setOriginCenter()
            spriteGem!!.draw(batch)
        } else {
            spriteGem!!.setSize(2 * gemR, 2 * gemR)
            spriteGem!!.setOriginCenter()
            spriteGem!!.setPosition(
                gemBody!!.position.x - gemR,
                gemBody!!.position.y - gemR
            )
            spriteGem!!.draw(batch)
        }
    }

    private fun drawBall(r: Float) {
        spriteBall!!.setSize(2 * r, 2 * r)
        spriteBall!!.setOriginCenter()
        spriteBall!!.setPosition(ballBody!!.position.x - r, ballBody!!.position.y - r)
        spriteBall!!.rotation = ballBody!!.angle * MathUtils.radiansToDegrees
        spriteBall!!.draw(batch)
    }

    private fun drawHints(r: Float) {
        spriteHintCircle!!.setSize(3f * r, 3f * r)
        spriteHintCircle!!.setOriginCenter()
        spriteHintCircle!!.setPosition(
            ballBody!!.position.x - r * 1.5f,
            ballBody!!.position.y - r * 1.5f
        )
        spriteHintCircle!!.draw(batch)
        spriteHintArrow!!.setSize(1f, 2f)
        spriteHintArrow!!.setOriginCenter()
        spriteHintArrow!!.x = ballBody!!.position.x - 0.5f
        spriteHintArrow!!.draw(batch)
    }

    private fun drawScore() {
        batch!!.projectionMatrix = uiCam!!.combined
        batch!!.draw(
            spriteSideMonitor, 40f, uiCam!!.viewportHeight / 2f
                    + uiCam!!.viewportHeight / 5f, 100f, 80f
        )
        font70!!.draw(
            batch,
            String.format("%02d", score),
            55f,
            uiCam!!.viewportHeight / 2f + uiCam!!.viewportHeight / 5f + 60
        )
        batch!!.projectionMatrix = cam!!.combined
    }

    private fun gameOver() {
        batch!!.projectionMatrix = uiCam!!.combined

        //out of other two buttons
        spriteNextLesson!!.setSize(1f, 1f)
        spriteNextLesson!!.setOriginCenter()
        spriteNextLesson!!.setPosition(-100f, -100f)
        spritePlayMore!!.setSize(1f, 1f)
        spritePlayMore!!.setOriginCenter()
        spritePlayMore!!.setPosition(-100f, -100f)
        batch!!.draw(spriteGameOver, 0f, 0f, uiCam!!.viewportWidth, uiCam!!.viewportHeight)
        spriteReplay!!.setSize(279f, 78f)
        spriteReplay!!.setOriginCenter()
        spriteReplay!!.setPosition(
            uiCam!!.viewportWidth / 2f - spriteReplay!!.width / 2f,
            uiCam!!.viewportHeight / 2f + 100 - spriteReplay!!.height / 2f
        )
        spriteNextLesson2!!.setSize(279f, 78f)
        spriteNextLesson2!!.setOriginCenter()
        spriteNextLesson2!!.setPosition(
            uiCam!!.viewportWidth / 2f - spriteNextLesson2!!.width / 2f,
            uiCam!!.viewportHeight / 2f - 100 - spriteNextLesson2!!.height / 2f
        )
        spriteReplay!!.draw(batch)
        spriteNextLesson2!!.draw(batch)
        batch!!.projectionMatrix = cam!!.combined
    }

    private fun resetGame() {
        topOfBasket = false
        win = false
        groundFixTop!!.isSensor = true
        leftBody!!.fixtureList[0].isSensor = true
        rightBody!!.fixtureList[0].isSensor = true
        leftLine!!.fixtureList[0].isSensor = true
        rightLine!!.fixtureList[0].isSensor = true
        ballBody!!.world.destroyBody(ballBody)
        if (!moving) {
            ballRemain--
        }
        createBall()
    }

    override fun dispose() {
        Gdx.app.log("Basketball", "Disposing")
        
        //body dispose
        assMan.manager.dispose()
        font70?.dispose()
        batch?.dispose()
        //        debugRender.dispose();
        world.dispose()
    }

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        if (!data.isHint) {
            data.isHint = true
            flashHintAnim!!.pause()
            flashHintAnimUp!!.pause()
            flashHintAnim = null
            flashHintAnimUp = null
        }
        if (skipOption) {
            if (spritePlayMore!!.boundingRectangle.contains(x, y)) {
                Gdx.app.exit()
            } else if (spriteNextLesson!!.boundingRectangle.contains(x, y)) {
                ballShoot = 0
                skipOption = false
                resetGame()
            }
        } else if (skipOption2) {
            if (spritePlayMore!!.boundingRectangle.contains(x, y)) {
                Gdx.app.exit()
            } else if (spriteNextLesson!!.boundingRectangle.contains(x, y)) {
                skipOption2 = false
                resetGame()
            }
        } else if (gameOver) {
            if (spriteReplay!!.boundingRectangle.contains(x, y)) {
                Gdx.app.exit()
            } else if (spriteNextLesson2!!.boundingRectangle.contains(x, y)) {
                gameOver = false
                round = 1
                score = 0
                ballShoot = 0
                xpos = cam!!.viewportWidth / 2f
                retry++
                resetGame()
            }
        }
        point = Vector3()
        point!![x, y] = 0f // Translate to world coordinates.
        cam!!.unproject(point)
        wasTouched = ballBody!!.fixtureList.first().testPoint(point!!.x, point!!.y)
        return true
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        return false
    }

    override fun longPress(x: Float, y: Float): Boolean {
        return false
    }

    /**
     * @param velocityX
     * @param velocityY
     * @param button
     * @return
     * @Task :- Siwpe the ball towards the basket
     */
    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        var angle =
            (-Math.toDegrees(Math.atan2(velocityY.toDouble(), velocityX.toDouble()))).toFloat()
        if (angle > 90) {
            angle = 90 + (angle - 90) / 3
        } else if (angle < 90) {
            angle = 90 - (90 - angle) / 3
        }
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX > 0) {
            } else {
            }
        } else {
            if (velocityY > 0) {
            } else {
                if (wasTouched && !gameOver && data.isHint && !skipOption && !shoot) {
                    lunchBall(angle)
                }
            }
        }
        return false
    }

    /**
     * Thow ball to calculated angle and play shoot sound
     *
     * @param angle
     */
    private fun lunchBall(angle: Float) {
        shoot = true
        ballShoot++
        val speed = Vector2(ballBody!!.position.x, ballBody!!.position.y)
            .dst(Vector2(point2!!.x, point2!!.y))
        if (data.isSound) {
            shootSound!!.play()
        }
        val initialVelocity = Vector2(
            Math.min(8f, Math.max(7.5f, speed * 2)),
            Math.min(8f, Math.max(7.5f, speed * 2))
        )
        initialVelocity.rotate(angle - 45)
        ballBody!!.setLinearVelocity(initialVelocity.x, initialVelocity.y)
        ballBody!!.angularVelocity = angle + 2 - 90
        //tween animation for ball size decrease
        Tween.to(ballBody!!.fixtureList[0].shape, ShapeAccessor.TYPE_RADIUS, 1f)
            .target(0.3f)
            .ease(TweenEquations.easeNone)
            .start(manager)
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        return false
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        point2 = Vector3()
        point2!![x, y] = 0f
        cam!!.unproject(point2)
        return false
    }

    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        return false
    }

    override fun pinch(
        initialPointer1: Vector2,
        initialPointer2: Vector2,
        pointer1: Vector2,
        pointer2: Vector2
    ): Boolean {
        return false
    }

    override fun pinchStop() {}
    override fun beginContact(contact: Contact) {
        val A = contact.fixtureA
        val B = contact.fixtureB
        if (A.body.userData === "BASKET") {
            if (topOfBasket) {
                basketContact()
            }
        } else if (B.body.userData === "BASKET") {
            if (topOfBasket) {
                basketContact()
            }
        } else if (!A.isSensor && !B.isSensor) {
            if (A.body.userData != null) {
                if (A.body.userData === "beep") {
                    dropSoundPlay()
                }
            } else if (B.body.userData != null) {
                if (B.body.userData === "beep") {
                    dropSoundPlay()
                }
            }
        }
    }

    private fun gemAnimationContact() {
        if (round == 2 && !data.isBack) {
            drawGem = true
            gemAnimation = Tween.to(spriteGem, SpriteAccessor.TYPE_XY, 2.0f)
                .target(cam!!.viewportWidth, cam!!.viewportHeight)
                .ease(TweenEquations.easeInOutCubic)
                .setCallback { type: Int, source: BaseTween<*>? ->
                    drawGem = false
                    gem++
                }
                .setCallbackTriggers(TweenCallback.COMPLETE)
                .start(manager)
        }
    }

    private fun dropSoundPlay() {
        if (data.isSound && !moving) {
            dropSound1!!.play()
        }
    }

    private fun basketContact() {
        win = true
        if (data.isSound) {
            croowedSound!!.play()
        }
        score++
        drawEmoji = true
        number = getRandomNumberInRange(0, 4)
        winEmoji[number]!!.x = spriteTopMonitor!!.x + 1f
        emoJiAnimatino = Tween.to(winEmoji[number], SpriteAccessor.TYPE_X, 1.0f)
            .target(spriteTopMonitor!!.width - 0.5f)
            .ease(TweenEquations.easeInOutCubic)
            .setCallback { type: Int, source: BaseTween<*>? -> drawEmoji = false }
            .setCallbackTriggers(TweenCallback.COMPLETE)
            .start(manager)
    }

    override fun endContact(contact: Contact) {
        val A = contact.fixtureA
        val B = contact.fixtureB
        if (A.body.userData === "basketline") {
            if (!topOfBasket) {
                leftBody!!.fixtureList[0].isSensor = false
                rightBody!!.fixtureList[0].isSensor = false
                leftLine!!.fixtureList[0].isSensor = false
                rightLine!!.fixtureList[0].isSensor = false
                topOfBasket = true
            } else {
                groundFixTop!!.isSensor = false
            }
        } else if (B.body.userData === "basketline") {
            if (!topOfBasket) {
                leftBody!!.fixtureList[0].isSensor = false
                rightBody!!.fixtureList[0].isSensor = false
                leftLine!!.fixtureList[0].isSensor = false
                rightLine!!.fixtureList[0].isSensor = false
                topOfBasket = true
            } else {
                groundFixTop!!.isSensor = false
            }
        }
        if (A.body.userData === "GEM") {
            if (topOfBasket && win) {
                gemAnimationContact()
            }
        } else if (B.body.userData === "GEM") {
            if (topOfBasket && win) {
                gemAnimationContact()
            }
        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {}
    override fun postSolve(contact: Contact, impulse: ContactImpulse) {}

    companion object {
        private const val BALL_RADIOS = 0.5f
        private const val GROUND_Y = 0.5f
        private const val RIM_RADIOS = 0.02f
        private const val UPPER_GROUND_Y = 3f
        const val STEEL = 0
        const val WOOD = 1
        const val RUBBER = 2
        const val STONE = 3
        private fun makeFixture(material: Int, shape: Shape): FixtureDef {
            val fixtureDef = FixtureDef()
            fixtureDef.shape = shape
            when (material) {
                0 -> {
                    fixtureDef.density = 1f
                    fixtureDef.friction = 0.3f
                    fixtureDef.restitution = 0.1f
                }
                1 -> {
                    fixtureDef.density = 0.5f
                    fixtureDef.friction = 0.7f
                    fixtureDef.restitution = 0.3f
                }
                2 -> {
                    fixtureDef.density = 1f
                    fixtureDef.friction = 0f
                    fixtureDef.restitution = 1f
                }
                3 -> {
                    fixtureDef.density = 1f
                    fixtureDef.friction = 0.9f
                    fixtureDef.restitution = 0.01f
                    fixtureDef.density = 7f
                    fixtureDef.friction = 0.5f
                    fixtureDef.restitution = 0.3f
                }
                else -> {
                    fixtureDef.density = 7f
                    fixtureDef.friction = 0.5f
                    fixtureDef.restitution = 0.3f
                }
            }
            return fixtureDef
        }

        private fun getRandomNumberInRange(min: Int, max: Int): Int {
            val r = Random()
            return r.nextInt(max - min + 1) + min
        }
    }

    init {
        manager = TweenManager()
        Tween.registerAccessor(Sprite::class.java, SpriteAccessor())
        Tween.registerAccessor(Shape::class.java, ShapeAccessor())
        data = instance
    }
}