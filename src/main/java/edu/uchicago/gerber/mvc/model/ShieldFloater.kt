package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.Game
import java.awt.Color

class ShieldFloater : Floater() {

    companion object {
        //spawn every 25 seconds
        const val SPAWN_SHIELD_FLOATER = Game.FRAMES_PER_SECOND * 25
    }
    init {
        color = Color.CYAN
        expiry = 260
    }


}
