package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.CommandCenter
import edu.uchicago.gerber.mvc.controller.Game
import edu.uchicago.gerber.mvc.controller.Sound
import java.awt.Color
import java.util.*

class NukeFloater : Floater() {
    init {
        color = Color.YELLOW
        expiry = 120
    }

    companion object {
        const val SPAWN_NUKE_FLOATER = Game.FRAMES_PER_SECOND * 50
    }

    override fun remove(list: MutableList<Movable>) {
        super.remove(list)
        //if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
        if (expiry > 0) {
            Sound.playSound("nuke-up.wav")
            CommandCenter.falcon.nukeMeter = Falcon.MAX_NUKE
        }
    }
}
