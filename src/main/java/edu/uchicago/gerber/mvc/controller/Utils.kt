package edu.uchicago.gerber.mvc.controller

import edu.uchicago.gerber.mvc.model.prime.PolarPoint
import java.awt.Point
import java.util.*
import java.util.function.BiFunction
import java.util.stream.Collectors

object Utils {
    ////////////////////////////////////////////////////////////////////
    //Utility methods for transforming cartesian2Polar, pointsListToArray, etc.
    ////////////////////////////////////////////////////////////////////
    fun cartesianToPolar(pntCartesians: Array<Point>): Array<PolarPoint> {

        val cartToPolarTransform = BiFunction { pnt: Point, hyp: Double ->
            PolarPoint( //this is r from PolarPoint(r,theta).
                hypotFunction(pnt.x.toDouble(), pnt.y.toDouble()) / hyp,  //r is relative to the largestHypotenuse
                //this is theta from PolarPoint(r,theta)
                Math.toDegrees(Math.atan2(pnt.y.toDouble(), pnt.x.toDouble())) * Math.PI / 180
            )
        }


        //determine the largest hypotenuse
        var largestHypotenuse = 0.0
        for (pnt in pntCartesians){
            if (hypotFunction(pnt.x.toDouble(), pnt.y.toDouble()) > largestHypotenuse)
                largestHypotenuse = hypotFunction(pnt.x.toDouble(), pnt.y.toDouble())
        }


        //we must make hypotenuse final to pass into a stream.
        val hyp = largestHypotenuse
        return Arrays.stream(pntCartesians)
            .map { pnt: Point -> cartToPolarTransform.apply(pnt, hyp) }
            .collect(Collectors.toList())
            .toTypedArray()
    }


    @JvmStatic
    fun pointsListToArray(listPoints: List<Point>): Array<Point> {
            return  listPoints.toTypedArray()
    }

    //private helper method
    private fun hypotFunction(sideX: Double, sideY: Double): Double {
        return Math.sqrt(Math.pow(sideX, 2.0) + Math.pow(sideY, 2.0))
    }
}


