package edu.uchicago.gerber.mvc.controller

import edu.uchicago.gerber.mvc.model.*
import java.awt.Dimension
import java.util.*

object CommandCenter {


    enum class Universe {
        FREE_FLY,
        CENTER,
        BIG,
        HORIZONTAL,
        VERTICAL,
        DARK
    }

    var universe: Universe? = null
    var numFalcons = 0
     var level = 0
     var score: Long = 0L
     var paused = false
    var themeMusic = false
    var radar = false //to toggle on/off the mini-map

    var frame: Long = 0L
    private val miniDimHash: MutableMap<Universe, Dimension> = mutableMapOf()
    private val miniMap: MiniMap = MiniMap()


    //the falcon should always point to this object on the heap
     val falcon = Falcon()

    //lists containing our movables
     val movDebris: MutableList<Movable> = LinkedList()
     val movFriends: MutableList<Movable> = LinkedList()
     val movFoes: MutableList<Movable> = LinkedList()
     val movFloaters: MutableList<Movable> = LinkedList()

     val opsQueue = GameOpsQueue()



    fun getUniDim(): Dimension? {
        return miniDimHash[universe]
    }

    fun isFalconPositionFixed(): Boolean {
        return universe != Universe.FREE_FLY
    }

    fun initGame() {
        clearAll()
        generateStarField()
        setDimHash()
        level = 0
        score = 0
        paused = false
        //set to one greater than number of falcons lives in your game as decrementFalconNumAndSpawn() also decrements
        numFalcons = 4
        falcon.decrementFalconNumAndSpawn()
        opsQueue.enqueue(falcon, GameOp.Action.ADD)
        opsQueue.enqueue(miniMap, GameOp.Action.ADD)
    }

    fun setDimHash(){
        miniDimHash[Universe.FREE_FLY] = Dimension(1, 1)
        miniDimHash[Universe.CENTER] = Dimension(1, 1)
        miniDimHash[Universe.BIG] = Dimension(2, 2)
        miniDimHash[Universe.HORIZONTAL] = Dimension(3, 1)
        miniDimHash[Universe.VERTICAL] = Dimension(1, 3)
        miniDimHash[Universe.DARK] = Dimension(4, 4)
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

