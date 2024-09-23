package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.CommandCenter
import edu.uchicago.gerber.mvc.controller.Game
import edu.uchicago.gerber.mvc.model.Movable.Team
import edu.uchicago.gerber.mvc.model.prime.AspectRatio
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point


/**
 * Inspired by Michael Vasiliou's Sinistar, winner of Java game contest 2016.
 */
class MiniMap : Sprite() {
    //size of mini-map as percentage of screen (game dimension)
    private val MINI_MAP_PERCENT = 0.31

    //used to adjust non-square universes. Set in draw()
     var aspectRatio: AspectRatio? = null
     val PUMPKIN = Color(200, 100, 50)
     val LIGHT_GRAY = Color(200, 200, 200)

    init {
        team = Team.DEBRIS
        center = Point(0, 0)
    }

    override fun move() {}
    override fun draw(g: Graphics) {

        //controlled by the A-key
        if (!CommandCenter.radar) return

        //exclude ordinals 0 and 1 (the small universes)
        //if (CommandCenter.getInstance().getUniverse().ordinal() < 2) return;

        //get the aspect-ratio which is used to adjust for non-square universes
        aspectRatio = CommandCenter.getUniDim()?.let { aspectAdjustedRatio(it) }

        //scale to some percent of game-dim
        val miniWidth = Math.round(MINI_MAP_PERCENT * Game.DIM.width * aspectRatio!!.width).toInt()
        val miniHeight = Math.round(MINI_MAP_PERCENT * Game.DIM.height * aspectRatio!!.height).toInt()

        //gray bounding box (entire universe)
        g.color = Color.DARK_GRAY
        g.drawRect(
            0,
            0,
            miniWidth,
            miniHeight
        )


        //draw the view-portal box
        g.color = Color.DARK_GRAY
        val miniViewPortWidth: Int = miniWidth / CommandCenter.getUniDim()!!.width
        val miniViewPortHeight: Int = miniHeight / CommandCenter.getUniDim()!!.height
        g.drawRect(
            0,
            0,
            miniViewPortWidth,
            miniViewPortHeight
        )


        //draw debris radar-blips.
        CommandCenter.movDebris.forEach { mov ->
            g.color = Color.DARK_GRAY
            val translatedPoint = translatePoint(mov.myCenter())
            g.fillOval(translatedPoint.x - 1, translatedPoint.y - 1, 2, 2)
        }

        //draw foe (asteroids) radar-blips
        CommandCenter.movFoes.forEach { mov ->
            if (mov !is Asteroid) return@forEach
            val asteroid: Asteroid = mov
            g.color = LIGHT_GRAY
            val translatedPoint = translatePoint(asteroid.center)
            when (asteroid.size) {
                0 -> g.fillOval(translatedPoint.x - 3, translatedPoint.y - 3, 6, 6)
                1 -> g.drawOval(translatedPoint.x - 3, translatedPoint.y - 3, 6, 6)
                2 -> g.drawOval(translatedPoint.x - 2, translatedPoint.y - 2, 4, 4)
            }
        }


        //draw floater radar-blips
        CommandCenter.movFloaters.forEach { mov ->
            g.color = if (mov is NukeFloater) Color.YELLOW else Color.CYAN
            val translatedPoint = translatePoint(mov.myCenter())
            g.fillRect(translatedPoint.x - 2, translatedPoint.y - 2, 4, 4)
        }


        //draw friend radar-blips
        CommandCenter.movFriends.forEach { mov ->
            val color: Color
            color =
                if (mov is Falcon && CommandCenter.falcon.shield > 0
                ) Color.CYAN else if (mov is Nuke) Color.YELLOW else PUMPKIN
            g.color = color
            val translatedPoint = translatePoint(mov.myCenter())
            g.fillOval(translatedPoint.x - 2, translatedPoint.y - 2, 4, 4)
        }
    }

    //this function takes a center-point of a movable and scales it to display the blip on the mini-map.
    //Since Java's draw origin (0,0) is at the top-left, points will translate up and left.
    private fun translatePoint(point: Point): Point {
        return Point(
            Math.round(
                MINI_MAP_PERCENT * point.x / CommandCenter.getUniDim()!!.width * aspectRatio!!.width
            )
                .toInt(),
            Math.round(
                MINI_MAP_PERCENT * point.y / CommandCenter.getUniDim()!!.height * aspectRatio!!.height
            )
                .toInt()
        )
    }

    //the purpose of this method is to adjust the aspect of non-square universes
    private fun aspectAdjustedRatio(universeDim: Dimension): AspectRatio {
        return if (universeDim.width == universeDim.height) {
            AspectRatio(1.0, 1.0)
        } else if (universeDim.width > universeDim.height) {
            val wMultiple = universeDim.width.toDouble() / universeDim.height
            AspectRatio(wMultiple, 1.0).scale(0.5)
        } else {
            val hMultiple = universeDim.height.toDouble() / universeDim.width
            AspectRatio(1.0, hMultiple).scale(0.5)
        }
    }
}

