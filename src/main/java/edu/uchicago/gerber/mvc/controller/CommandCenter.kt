package edu.uchicago.gerber.mvc.controller

import edu.uchicago.gerber.mvc.model.*
import edu.uchicago.gerber.mvc.model.Star
import java.awt.Point
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

object CommandCenter {

     var numFalcons = 0
     var level = 0
     var score: Long = 0L
     var paused = false
     var muted = true
     var frame: Long = 0L

    //the falcon should always point to this object on the heap
     val falcon = Falcon()

    //lists containing our movables
     val movDebris: MutableList<Movable> = LinkedList()
     val movFriends: MutableList<Movable> = LinkedList()
     val movFoes: MutableList<Movable> = LinkedList()
     val movFloaters: MutableList<Movable> = LinkedList()

     val opsQueue = GameOpsQueue()

    //for sound playing. Limit the number of threads to 5 at a time.
     val soundExecutor = Executors.newFixedThreadPool(5) as ThreadPoolExecutor


    fun initGame() {
        clearAll()
        generateStarField()
        level = 0
        score = 0
        paused = false
        //set to one greater than number of falcons lives in your game as initFalconAndDecrementNum() also decrements
        numFalcons = 4
        initFalconAndDecrementFalconNum()
        opsQueue.enqueue(falcon, GameOp.Action.ADD)
    }

    private fun generateStarField() {
        var count = 100
        while (count-- > 0) {
            opsQueue.enqueue(Star(), GameOp.Action.ADD)
        }
    }

     fun initFalconAndDecrementFalconNum() {
        numFalcons -= 1
        if (isGameOver()) return
        Sound.playSound("shipspawn.wav")
        falcon.showLevel = 0
        falcon.maxSpeedAttained = false
        falcon.nukeMeter = 0
        falcon.radius = Falcon.MIN_RADIUS
        falcon.shield = Falcon.SPAWN_INIT_VALUE
        falcon.invisible = Falcon.SPAWN_INIT_VALUE / 4
        //put falcon in the middle of the game-space
        falcon.center = Point(Game.DIM.width / 2, Game.DIM.height / 2)
        //random number 0-360 in steps of 9 (DEGREE_STEP)
        falcon.orientation = Game.R.nextInt(40) * Falcon.DEGREE_STEP
        falcon.deltaX = 0.0
        falcon.deltaY = 0.0
    }

     fun clearAll() {
        movDebris.clear()
        movFriends.clear()
        movFoes.clear()
        movFloaters.clear()
    }

    fun incrementFrame() {
        frame = if (frame < Long.MAX_VALUE) frame + 1 else 0
    }


    //if the number of falcons is zero, then game over
    fun isGameOver(): Boolean {
        return numFalcons < 1
    }


}

