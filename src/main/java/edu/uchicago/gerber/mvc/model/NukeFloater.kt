package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.Game
import java.awt.Color

class NukeFloater : Floater() {
    init {
        color = Color.YELLOW
        expiry = 120
    }

    companion object {
        const val SPAWN_NUKE_FLOATER = Game.FRAMES_PER_SECOND * 50
    }
}
