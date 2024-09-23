package edu.uchicago.gerber.mvc.model.prime


class AspectRatio (var width: Double, var height: Double) {

    fun scale(scale: Double): AspectRatio {
        this.height = (height * scale)
        this.width = (width * scale)
        return this
    }
}
