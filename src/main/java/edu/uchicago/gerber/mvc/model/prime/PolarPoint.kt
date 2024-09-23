package edu.uchicago.gerber.mvc.model.prime

data class PolarPoint(
    // corresponds to the hypotenuse in cartesean, number between 0 and 1
    val r: Double,
    //degrees in radians, number between 0 and 6.283
    val theta: Double) {

    fun compareTheta(other: PolarPoint): Int {
        return this.theta.compareTo(other.theta)
    }
}

