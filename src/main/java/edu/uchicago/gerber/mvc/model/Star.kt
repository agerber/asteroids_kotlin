package edu.uchicago.gerber.mvc.model

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
       //do nothing
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

    override fun add(list: MutableList<Movable>) {
        list.add(this)
    }

    override  fun remove(list: MutableList<Movable>) {
        list.remove(this)
    }


}
