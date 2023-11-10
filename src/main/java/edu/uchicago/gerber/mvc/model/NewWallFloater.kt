package edu.uchicago.gerber.mvc.model

import edu.uchicago.gerber.mvc.controller.CommandCenter
import edu.uchicago.gerber.mvc.controller.Game
import edu.uchicago.gerber.mvc.controller.Sound
import java.awt.Color
import java.awt.Point
import java.util.*

class NewWallFloater : Floater() {

    companion object {
        //spawn every 40 seconds
        const val SPAWN_NEW_WALL_FLOATER = Game.FRAMES_PER_SECOND * 40
    }
    init {
        color = Color(186, 0, 22)
        expiry = 230
    }

    override fun remove(list: MutableList<Movable>) {
        super.remove(list)
        //if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
        if (expiry > 0) {
            Sound.playSound("insect.wav")
            buildWall()
        }

    }

    private fun buildWall() {
        val BRICK_SIZE = Game.DIM.width / 30
        val ROWS = 2
        val COLS = 20
        val X_OFFSET = BRICK_SIZE * 5
        val Y_OFFSET = 50
        for (nCol in 0 until COLS) {
            for (nRow in 0 until ROWS) {
                CommandCenter.opsQueue.enqueue(
                    Brick(
                        Point(nCol * BRICK_SIZE + X_OFFSET, nRow * BRICK_SIZE + Y_OFFSET),
                        BRICK_SIZE
                    ),
                    GameOp.Action.ADD
                )
            }
        }
    }


}
