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
        //set to one greater than number of falcons lives in your game as decrementFalconNumAndSpawn() also decrements
        numFalcons = 4
        falcon.decrementFalconNumAndSpawn()
        opsQueue.enqueue(falcon, GameOp.Action.ADD)
    }

    private fun generateStarField() {
        var count = 100
        while (count-- > 0) {
            opsQueue.enqueue(Star(), GameOp.Action.ADD)
        }
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

