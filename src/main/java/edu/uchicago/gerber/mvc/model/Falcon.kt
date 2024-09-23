package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.*
import edu.uchicago.gerber.mvc.model.Movable.Team
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.image.BufferedImage


class Falcon : Sprite() {
    // ==============================================================
    // FIELDS 
    // ==============================================================
    companion object {
        const val DEGREE_STEP = 9
        const val SPAWN_INIT_VALUE = 48
        const val MAX_SHIELD = 200
        const val MAX_NUKE = 600
        const val MIN_RADIUS = 28
        const val MAX_VEL = 39


    }
     enum class ImageState {
         FALCON_INVISIBLE, //for pre-spawning
         FALCON, //normal ship
         FALCON_THR, //normal ship thrusting
         FALCON_PRO, //protected ship (green)
         FALCON_PRO_THR //protected ship (green) thrusting
     }

    var thrusting = false
    var shield = 0
    var invisible = 0
    var showLevel = 0
    var maxSpeedAttained = false
    var nukeMeter = 0


    enum class TurnState {
        IDLE, LEFT, RIGHT
    }

    var turnState = TurnState.IDLE

     // ==============================================================
    // CONSTRUCTOR 
    // ==============================================================
    init {
        team = Team.FRIEND
        //this is the size (radius) of the falcon
        radius = MIN_RADIUS

        val rasterMap: MutableMap<Any, BufferedImage?> = HashMap()
        rasterMap[ImageState.FALCON] = ImageLoader.getImage("/imgs/fal/falcon125.png")
        rasterMap[ImageState.FALCON_THR] = ImageLoader.getImage("/imgs/fal/falcon125_thr.png")
        rasterMap[ImageState.FALCON_PRO] = ImageLoader.getImage("/imgs/fal/falcon125_PRO.png")
        rasterMap[ImageState.FALCON_PRO_THR] = ImageLoader.getImage("/imgs/fal/falcon125_PRO_thr.png")
        rasterMap[ImageState.FALCON_INVISIBLE] = null
        this.rasterMap = rasterMap

    }




    // ==============================================================
    // METHODS 
    // ==============================================================
    override fun move() {


        //only call super.move() if falcon is not fixed
        if (!CommandCenter.isFalconPositionFixed()) super.move()

        if (invisible > 0) invisible--
        if (shield > 0) shield--
        if (nukeMeter > 0) nukeMeter--
        //The falcon is a convenient place to decrement this variable as the falcon
        //move() method is being called every frame (~40ms); and the falcon reference is never null.
        if (showLevel > 0) showLevel--



        //apply some thrust vectors using trig.
        val THRUST = 0.85
        if (thrusting) {
            val adjustX = (Math.cos(Math.toRadians(orientation.toDouble()))
                    * THRUST)
            val adjustY = (Math.sin(Math.toRadians(orientation.toDouble()))
                    * THRUST)


            //Absolute velocity is the hypotenuse of deltaX and deltaY
            val absVelocity = Math.sqrt(Math.pow(deltaX + adjustX, 2.0) + Math.pow(deltaY + adjustY, 2.0)).toInt()

            if (absVelocity < MAX_VEL){

                deltaX = deltaX + adjustX
                deltaY = deltaY + adjustY
                radius = MIN_RADIUS + absVelocity / 3
                maxSpeedAttained = false
            } else {
                maxSpeedAttained = true
            }

        }

        when (turnState) {
            TurnState.LEFT -> {
                orientation =  if (orientation <= 0) 351 else orientation - DEGREE_STEP
            }

            TurnState.RIGHT -> {
               orientation =  if (orientation >= 360) 9 else orientation + DEGREE_STEP
            }

            else -> {
                //do nothing
            }

        }

    }

    private fun drawNukeHalo(g: Graphics) {
        if (invisible > 0) return
        g.color = Color.YELLOW
        g.drawOval(
            center.x - radius + 10, center.y - radius + 10, radius * 2 - 20,
            radius * 2 - 20
        )
    }

    //raster and vector implementation of draw()
    override fun draw(g: Graphics) {
        if (nukeMeter > 0) drawNukeHalo(g)
        //set image-state
        val imageState: ImageState
        if (invisible > 0){
            imageState = ImageState.FALCON_INVISIBLE
        }
        else if (shield > 0) {
          imageState =  if (thrusting) ImageState.FALCON_PRO_THR else ImageState.FALCON_PRO
            //you can also combine vector elements and raster elements
            drawShield(g)
        } else { //not protected
            imageState =   if (thrusting) ImageState.FALCON_THR else ImageState.FALCON
        }

        //cast (widen the aperture of) the graphics object to gain access to methods of Graphics2D
        //and render the image according to the image-state
        renderRaster((g as Graphics2D), rasterMap[imageState])


    }

     private fun drawShield(g: Graphics) {
         g.color = Color.CYAN
         g.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2)
     }



     override fun removeFromGame(list: MutableList<Movable>) {
         //The falcon is never actually removed from the game-space; instead we decrement numFalcons
         //only execute the decrementFalconNumAndSpawn() method if shield is down.
         if (shield == 0) decrementFalconNumAndSpawn()
     }

    fun decrementFalconNumAndSpawn() {
        CommandCenter.numFalcons -= 1
        if (CommandCenter.isGameOver()) return
        SoundLoader.playSound("shipspawn.wav")
        showLevel = 0
        maxSpeedAttained = false
        nukeMeter = 0
        radius = Falcon.MIN_RADIUS
        shield = Falcon.SPAWN_INIT_VALUE
        invisible = Falcon.SPAWN_INIT_VALUE / 5
        //put falcon in the middle of the game-space
        center = Point(Game.DIM.width / 2, Game.DIM.height / 2)
        //random number 0-360 in steps of 9 (DEGREE_STEP)
        orientation = Game.R.nextInt(40) * Falcon.DEGREE_STEP
        deltaX = 0.0
        deltaY = 0.0
    }



 }
