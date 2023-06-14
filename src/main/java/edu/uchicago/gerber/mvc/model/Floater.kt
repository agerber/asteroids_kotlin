package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.Utils.pointsListToArray
import edu.uchicago.gerber.mvc.model.Movable.Team
import java.awt.Color
import java.awt.Graphics
import java.awt.Point

abstract class Floater : Sprite() {
    init {
        team = Team.FLOATER
        //default values, all of which can be overridden in the extending concrete classes
        expiry = 250
        color = Color.WHITE
        radius = 50
        //set random DeltaX
        deltaX = somePosNegValue(10).toDouble()
        //set random DeltaY
        deltaY = somePosNegValue(10).toDouble()
        //set random spin
        spin = somePosNegValue(10)

        //cartesian points which define the shape of the polygon
        val listPoints: MutableList<Point> = ArrayList()
        listPoints.add(Point(5, 5))
        listPoints.add(Point(4, 0))
        listPoints.add(Point(5, -5))
        listPoints.add(Point(0, -4))
        listPoints.add(Point(-5, -5))
        listPoints.add(Point(-4, 0))
        listPoints.add(Point(-5, 5))
        listPoints.add(Point(0, 4))
        cartesians = pointsListToArray(listPoints)
    }

    override fun draw(g: Graphics) {
        renderVector(g)
    }
}
