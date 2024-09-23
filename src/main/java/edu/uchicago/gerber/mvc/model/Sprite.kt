package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.CommandCenter
import edu.uchicago.gerber.mvc.controller.Game
import edu.uchicago.gerber.mvc.controller.Utils
import edu.uchicago.gerber.mvc.model.Movable.Team
import edu.uchicago.gerber.mvc.model.prime.PolarPoint
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.*
import java.util.function.Function
import javax.imageio.ImageIO


abstract class Sprite : Movable {

    var deltaX: Double = 0.0
    var deltaY: Double = 0.0
    var radius: Int = 0
    var center: Point
    var orientation: Int = 0
    var expiry: Int = 0
    var spin: Int = 0
    var color: Color = Color.WHITE

    //all these will be initialized in the subclasses and have no default values
    //used for vectors
    lateinit var cartesians: Array<Point>

    //used for rasters
    lateinit var rasterMap: Map<Any, BufferedImage?>
    lateinit var team: Team

    init {
        center = (Point(Game.R.nextInt(Game.DIM.width),
                Game.R.nextInt(Game.DIM.height)))
    }

    //contract methods of interface used for collission detection. We can not use getCenter or getRadius etc. b/c that
    //would conflict with kotlin's automatic assignment of getters and setters, so we use myCenter() and myRadius()
    override fun myCenter(): Point {
      return  center
    }

    override fun myRadius(): Int {
       return  radius
    }

    override fun myTeam(): Team {
      return  team
    }

    override fun move() {

        //The following code block just keeps the sprite inside the bounds of the frame.
        //To ensure this behavior among all sprites in your game, make sure to call super.move() in extending classes
        // where you need to override the move() method.
        //A scalar(larger than 1) allows the sprite to move beyond the bounds of the game-screen dimension


        //The following code block just keeps the sprite inside the bounds of the universe.
        //To ensure this behavior among all sprites in your game, make sure to call super.move() in extending classes
        // where you need to override the move() method.

        //A scalar(larger than 1) allows the sprite to move beyond the bounds of the game-screen dimension
        val scalarX: Int = CommandCenter.getUniDim()!!.width
        val scalarY: Int = CommandCenter.getUniDim()!!.height

        //right-bounds reached
        if (center.x > scalarX *  Game.DIM.width) {
            center.x = 1
            //left-bounds reached
        } else if (center.x < 0) {
            center.x = scalarX * Game.DIM.width -1
            //bottom-bounds reached
        } else if (center.y >  scalarY * Game.DIM.height) {
            center.y = 1
            //top-bounds reached
        } else if (center.y < 0) {
            center.y = scalarY * Game.DIM.height - 1
            //in-bounds
        } else {
            var newXPos = center.x.toDouble()
            var newYPos = center.y.toDouble()
            //if falcon-fixed, move the sprite in the opposite direction of the falcon to create centered-play
            //if falcon-fixed, move the sprite in the opposite direction of the falcon to create centered-play
            if (CommandCenter.isFalconPositionFixed()) {
                newXPos -= CommandCenter.falcon.deltaX
                newYPos -= CommandCenter.falcon.deltaY
            }
            center.x = Math.round(newXPos + deltaX).toInt()
            center.y = Math.round(newYPos + deltaY).toInt()


        }

        //expire (decrement expiry) on short-lived objects only
        //the default value of expiry is zero, so this block will only apply to expiring sprites
        if (expiry > 0) expire()

        //if spin is not zero, adjust the orientation accordingly
        //the default value of spin is zero, therefore this block will only be called on spinning objects
        if (spin != 0) orientation += spin
    }



    private fun expire() {

        //if a short-lived sprite has an expiry of one, it commits suicide by enqueuing itself (this) onto the
        //opsList with an operation of REMOVE
        if (expiry == 1) {
            CommandCenter.opsQueue.enqueue(this, GameOp.Action.REMOVE)
        }
        //and then decrements in all cases
        expiry--
    }


    protected fun somePosNegValue(seed: Int): Int {
        var randomNumber = Game.R.nextInt(seed)
        if (randomNumber % 2 == 0) randomNumber = -randomNumber
        return randomNumber
    }

    protected fun renderVector(g: Graphics) {

        g.color = color
        // to render this Sprite, we need to, 1: convert raw cartesians to raw polars, 2: adjust polars
        // for orientation of sprite. Convert back to cartesians 3: adjust for center-point (location).
        // and 4: pass the cartesian-x and cartesian-y coords as arrays, along with length, to drawPolygon().

        //convert raw cartesians to raw polars
        val polars = Utils.cartesianToPolar(cartesians)

        //rotate raw polars given the orientation of the sprite. Then convert back to cartesians.
        val adjustForOrientation = Function { (r, theta): PolarPoint ->
            Point((r * radius
                    * Math.sin(Math.toRadians(orientation.toDouble())
                    + theta)).toInt(), (r * radius
                    * Math.cos(Math.toRadians(orientation.toDouble())
                    + theta)).toInt())
        }

        // adjust for the location (center-point) of the sprite.
        // the reason we subtract the y-value has to do with how Java plots the vertical axis for
        // graphics (from top to bottom)
        val adjustForLocation = Function { p: Point ->
            Point(
                    center.x + p.x,
                    center.y - p.y)
        }
        g.drawPolygon(
                Arrays.stream(polars)
                        .map(adjustForOrientation)
                        .map(adjustForLocation)
                        .map { pnt: Point -> pnt.x }
                        .mapToInt { obj: Int -> obj }
                        .toArray(),
                Arrays.stream(polars)
                        .map(adjustForOrientation)
                        .map(adjustForLocation)
                        .map { pnt: Point -> pnt.y }
                        .mapToInt { obj: Int -> obj }
                        .toArray(),
                polars.size)

        //for debugging center-point. Feel free to remove these two lines.
        //#########################################
        //g.color = Color.ORANGE
        //g.fillOval(center.x - 1, center.y - 1, 2, 2)
        //g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() *2, getRadius() *2);
        //#########################################
    }



    //https://www.tabnine.com/code/java/methods/java.awt.geom.AffineTransform/rotate
    protected open fun renderRaster(g2d: Graphics2D, bufferedImage: BufferedImage?) {

        if (bufferedImage == null) return

        val centerX: Int = center.x
        val centerY: Int = center.y
        val width: Int = radius * 2
        val height: Int = radius * 2
        val angleRadians = Math.toRadians(orientation.toDouble())
        val oldTransform = g2d.transform
        try {
            val scaleX = width * 1.0 / bufferedImage.width
            val scaleY = height * 1.0 / bufferedImage.height
            val affineTransform = AffineTransform(oldTransform)
            if (centerX != 0 || centerY != 0) {
                affineTransform.translate(centerX.toDouble(), centerY.toDouble())
            }
            affineTransform.scale(scaleX, scaleY)
            if (angleRadians != 0.0) {
                affineTransform.rotate(angleRadians)
            }
            affineTransform.translate(-bufferedImage.width / 2.0, -bufferedImage.height / 2.0)
            g2d.transform = affineTransform
            g2d.drawImage(bufferedImage, 0, 0, bufferedImage.width, bufferedImage.height, null)
        } finally {
            g2d.transform = oldTransform
        }
    }

    protected open fun loadGraphic(imgPath: String): BufferedImage? {
        val img: BufferedImage?
        img = try {
            ImageIO.read(Objects.requireNonNull(Sprite::class.java.getResourceAsStream(imgPath)))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
        return img
    }

    //default behavior for adding and removing objects from game space.
    //The 'list' parameter will be one of the following: movFriends, movFoes, movDebris, movFloaters.
    override fun addToGame(list: MutableList<Movable>) {
        list.add(this)
    }

   override fun removeFromGame(list: MutableList<Movable>) {
        list.remove(this)
    }


}
