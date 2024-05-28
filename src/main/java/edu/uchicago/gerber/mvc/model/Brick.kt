package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.CommandCenter
import edu.uchicago.gerber.mvc.controller.Sound
import edu.uchicago.gerber.mvc.model.Movable.Team
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.image.BufferedImage
import java.util.*

class Brick(upperLeftCorner: Point, size: Int) : Sprite() {
    companion object{
        private const val BRICK_IMAGE = 0
    }


    //The size of this brick is always square!
    //we use upperLeftCorner because that is the origin when drawing graphics in Java
    init {

        team = Team.FOE
        center = Point(upperLeftCorner.x + size / 2, upperLeftCorner.y + size / 2)
        radius = size / 2

        //As this sprite does not animate or change state, we could just store a BufferedImage as a member, but
        //since we already have a rasterMap in the Sprite class, we might as well be consistent for all raster sprites
        // and use it.
        val rasterMap: MutableMap<Any, BufferedImage?> = HashMap()
        rasterMap.put(BRICK_IMAGE, loadGraphic("/imgs/brick/Brick_Block100.png"))
        this.rasterMap = rasterMap
    }

    override fun draw(g: Graphics) {
        renderRaster((g as Graphics2D), rasterMap[BRICK_IMAGE])
        //if you uncomment these, you can see how collision works. Feel free to remove these two lines.
        //g.setColor(Color.LIGHT_GRAY);
        //g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() *2, getRadius() *2);
    }

    //the reason we override the move method is to skip the logic contained in super-class Sprite move() method
    //and gain slight performance
    override fun move() {
        //do NOT call super.move() and do nothing, a brick does not move.
    }

    override fun removeFromGame(list: MutableList<Movable>) {
        super.removeFromGame(list)
        CommandCenter.score = CommandCenter.score + 1000
        Sound.playSound("rock.wav")

    }
} //end class
