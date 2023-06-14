package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.model.Movable.Team
import lombok.Data
import java.awt.Color
import java.awt.Graphics

@Data
class Nuke(falcon: Falcon) : Sprite() {
    private var nukeState = 0


    init {
        center = falcon.center
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

    //a nuke is invincible until it collides 10 times
    override fun isProtected(): Boolean {
        return true
    }



    override fun move() {
        super.move()
        if (expiry % (EXPIRE / 6) == 0) nukeState++
        radius = when (nukeState) {
            0 -> 2
            1, 2, 3 -> radius + 16
            4, 5 -> radius - 22
            else -> radius - 22
        }
    }

    companion object {
        private const val EXPIRE = 60
    }
}
