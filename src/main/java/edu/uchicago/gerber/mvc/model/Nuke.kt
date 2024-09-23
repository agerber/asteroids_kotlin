package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.CommandCenter
import edu.uchicago.gerber.mvc.controller.Sound
import edu.uchicago.gerber.mvc.controller.SoundLoader
import edu.uchicago.gerber.mvc.model.Movable.Team
import lombok.Data
import java.awt.Color
import java.awt.Graphics
import java.awt.Point

@Data
class Nuke(falcon: Falcon) : Sprite() {
    private var nukeState = 0


    init {
        center = falcon.center.clone() as Point
        color = Color.YELLOW
        expiry = EXPIRE
        radius = 0
        team = Team.FRIEND
        val FIRE_POWER = 11.0
        val vectorX = Math.cos(Math.toRadians(falcon.orientation.toDouble())) * FIRE_POWER
        val vectorY = Math.sin(Math.toRadians(falcon.orientation.toDouble())) * FIRE_POWER

        //fire force: falcon inertia + fire-vector
        deltaX = falcon.deltaX + vectorX
        deltaY = falcon.deltaY + vectorY
    }

    override fun draw(g: Graphics) {
        g.color = color
        g.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2)
    }


    override fun move() {
        super.move()
        if (expiry % (EXPIRE / 6) == 0) nukeState++
        radius = when (nukeState) {
            0 -> 17
            1, 2, 3 -> radius + 8
            4, 5 -> radius - 11
            else -> radius - 11
        }
    }

    companion object {
        private const val EXPIRE = 60
    }

    override fun addToGame(list: MutableList<Movable>) {
        if (CommandCenter.falcon.nukeMeter > 0){
            list.add(this)
            SoundLoader.playSound("nuke.wav")
            CommandCenter.falcon.nukeMeter = 0
        }

    }

    override fun removeFromGame(list: MutableList<Movable>) {
        //if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
        if (expiry == 0)  list.remove(this)

    }
}
