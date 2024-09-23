package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.CommandCenter
import edu.uchicago.gerber.mvc.controller.Game
import edu.uchicago.gerber.mvc.controller.Sound
import edu.uchicago.gerber.mvc.controller.SoundLoader
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

    override fun removeFromGame(list: MutableList<Movable>) {
        super.removeFromGame(list)
        //if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
        if (expiry > 0) {
            SoundLoader.playSound("shieldup.wav")
            CommandCenter.falcon.shield = Falcon.MAX_SHIELD
        }
    }

}
