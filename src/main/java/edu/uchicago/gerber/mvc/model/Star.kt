package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.CommandCenter
import edu.uchicago.gerber.mvc.controller.Game
import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import java.util.*

class Star: Movable {

     var center: Point
     var color: Color

 init {
     center = (Point(
         Game.R.nextInt(Game.DIM.width),
         Game.R.nextInt(Game.DIM.height)))
     val bright = Game.R.nextInt(256)
     color = Color(bright, bright, bright)
 }

    override fun draw(g: Graphics) {
        g.color = color
        g.drawOval(center.x, center.y, myRadius(), myRadius())
    }


    override fun move() {
        //if falcon position is NOT fixed (e.g. FREE_FLY), return


        //if falcon position is NOT fixed (e.g. FREE_FLY), return
        if (!CommandCenter.isFalconPositionFixed()) return

        //else, falcon position is fixed, and the stars must move to orient player in falcon-fixed-play

        //right-bounds reached

        //else, falcon position is fixed, and the stars must move to orient player in falcon-fixed-play

        //right-bounds reached
        if (center.x > Game.DIM.width) {
            center.x = 1
            //left-bounds reached
        } else if (center.x < 0) {
            center.x = Game.DIM.width - 1
            //bottom-bounds reached
        } else if (center.y > Game.DIM.height) {
            center.y = 1
            //top-bounds reached
        } else if (center.y < 0) {
            center.y = Game.DIM.height - 1
            //in-bounds
        } else {
            //move star in opposite direction of falcon.
            center.x = Math.round(center.x - CommandCenter.falcon.deltaX).toInt()
            center.y = Math.round(center.y - CommandCenter.falcon.deltaY).toInt()
        }

    }


    override fun myCenter(): Point {
        return center
    }

    override fun myRadius(): Int {
       return 1
    }

    override fun myTeam(): Movable.Team {
        return Movable.Team.DEBRIS
    }

    override fun addToGame(list: MutableList<Movable>) {
        list.add(this)
    }

    override  fun removeFromGame(list: MutableList<Movable>) {
        list.remove(this)
    }


}
