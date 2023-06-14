package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.Game
import java.awt.Color

class NewWallFloater : Floater() {

    companion object {
        //spawn every 40 seconds
        const val SPAWN_NEW_WALL_FLOATER = Game.FRAMES_PER_SECOND * 40
    }
    init {
        color = Color(186, 0, 22)
        expiry = 230
    }


}
