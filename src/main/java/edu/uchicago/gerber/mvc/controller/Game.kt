package edu.uchicago.gerber.mvc.controller

import edu.uchicago.gerber.mvc.controller.Sound.playSound
import edu.uchicago.gerber.mvc.model.*
import edu.uchicago.gerber.mvc.model.Movable.Team
import edu.uchicago.gerber.mvc.view.GamePanel
import edu.uchicago.gerber.mvc.model.Nuke
import edu.uchicago.gerber.mvc.model.NukeFloater
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import javax.sound.sampled.Clip

fun main() {
    //typical Swing application start; we pass EventQueue a Runnable object.
    EventQueue.invokeLater(Game())
}
// ===============================================
// == This Game class is the CONTROLLER
// ===============================================
class Game : Runnable, KeyListener {

    private val gmpPanel: GamePanel
    private val animationThread: Thread

    //sounds
    private val clpThrust: Clip
    private val clpMusicBackground: Clip

    //STATIC CONTEXT
    companion object {
        //KEY CONSTANTS
        // p key
        const val PAUSE = 80
        // q key
        const val QUIT = 81
        // rotate left; left arrow
        const val LEFT = 37
        // rotate right; right arrow
        const val RIGHT = 39
        // thrust; up arrow
        const val UP = 38
        // s key
        const val START = 83
        // space key
        const val FIRE = 32
        // m-key mute
        const val MUTE = 77
        //n key
        const val NUKE = 78


        val DIM = Dimension(1100, 900) //the dimension of the game.
        //this is used throughout many classes.
        val R = Random()
        const val ANIMATION_DELAY = 40 // milliseconds between screen
        // updates (animation)
        const val FRAMES_PER_SECOND = 1000 / ANIMATION_DELAY

    }

    // ===============================================
    // ==CONSTRUCTOR
    // ===============================================
    init {
        gmpPanel = GamePanel(DIM)
        gmpPanel.addKeyListener(this) //Game object implements KeyListener
        clpThrust = Sound.clipForLoopFactory("whitenoise.wav")
        clpMusicBackground = Sound.clipForLoopFactory("music-background.wav")

        //fire up the animation thread
        animationThread = Thread(this) // pass the animation thread a runnable object, the Game object
        animationThread.start()
    }

    // Game implements runnable, and must have run method
    override fun run() {

        // lower animation thread's priority, thereby yielding to the "main" aka 'Event Dispatch'
        // thread which listens to keystrokes
        animationThread.priority = Thread.MIN_PRIORITY

        // and get the current time
        var lStartTime = System.currentTimeMillis()

        // this thread animates the scene
        while (Thread.currentThread() === animationThread) {
            gmpPanel.update(gmpPanel.graphics) // see GamePanel class
            checkCollisions()
            checkNewLevel()
            checkFloaters()
            CommandCenter.incrementFrame()

            // surround the sleep() in a try/catch block
            // this simply controls delay time between
            // the frames of the animation
            try {
                // The total amount of time is guaranteed to be at least ANI_DELAY long.  If processing (update) 
                // between frames takes longer than ANI_DELAY, then the difference between lStartTime - 
                // System.currentTimeMillis() will be negative, then zero will be the sleep time
                lStartTime += ANIMATION_DELAY.toLong()
                Thread.sleep(Math.max(0,
                        lStartTime - System.currentTimeMillis()))
            } catch (e: InterruptedException) {
                // do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
            }
        } // end while
    } // end run

    private fun checkCollisions() {
        var pntFriendCenter: Point
        var pntFoeCenter: Point
        var radFriend: Int
        var radFoe: Int

        //This has order-of-growth of O(n^2), there is no way around this.
        for (movFriend in CommandCenter.movFriends) {
            for (movFoe in CommandCenter.movFoes) {
                pntFriendCenter = movFriend.myCenter()
                pntFoeCenter = movFoe.myCenter()
                radFriend = movFriend.myRadius()
                radFoe = movFoe.myRadius()

                //detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < radFriend + radFoe) {
                    //remove the friend (so long as he is not protected)
                    if (!movFriend.isProtected()) {
                        CommandCenter.opsQueue.enqueue(movFriend, GameOp.Action.REMOVE)
                    }

                    //remove the foe
                    CommandCenter.opsQueue.enqueue(movFoe, GameOp.Action.REMOVE)
                    if (movFoe is Brick) {
                        CommandCenter.score = (CommandCenter.score + 1000)
                        playSound("rock.wav")
                    } else {
                        CommandCenter.score = (CommandCenter.score + 10)
                        playSound("kapow.wav")
                    }
                }
            } //end inner for
        } //end outer for

        //check for collisions between falcon and floaters. Order of growth of O(n) where n is number of floaters
        val pntFalCenter = CommandCenter.falcon.myCenter()
        val radFalcon = CommandCenter.falcon.myRadius()
        var pntFloaterCenter: Point
        var radFloater: Int
        for (movFloater in CommandCenter.movFloaters) {
            pntFloaterCenter = movFloater.myCenter()
            radFloater = movFloater.myRadius()

            //detect collision
            if (pntFalCenter.distance(pntFloaterCenter) < (radFalcon + radFloater)) {

                val clazz: Class<out Movable?> = movFloater.javaClass
                when (clazz.simpleName) {
                    "ShieldFloater" -> {
                        playSound("shieldup.wav")
                        CommandCenter.falcon.shield = Falcon.MAX_SHIELD
                    }

                    "NewWallFloater" -> {
                        playSound("wall.wav")
                        buildWall()
                    }

                    "NukeFloater" -> {
                        playSound("nuke-up.wav")
                        CommandCenter.falcon.nukeMeter = Falcon.MAX_NUKE
                    }
                }
                CommandCenter.opsQueue.enqueue(movFloater, GameOp.Action.REMOVE)

            } //end for
        }
        processGameOpsQueue()
    }//end meth

    private fun processGameOpsQueue() {

        //deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
        // mutating the movable linkedlists while iterating them above
        while (!CommandCenter.opsQueue.isEmpty()) {
            val gameOp = CommandCenter.opsQueue.dequeue()
            val mov = gameOp?.movable
            val action = gameOp?.action
            if (mov != null) {
                when (mov.myTeam()) {
                    Team.FOE -> if (action == GameOp.Action.ADD) {
                        CommandCenter.movFoes.add(mov)
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.movFoes.remove(mov)
                        if (mov is Asteroid) spawnSmallerAsteroidsOrDebris(mov)
                    }

                    Team.FRIEND -> if (action == GameOp.Action.ADD) {
                        CommandCenter.movFriends.add(mov)
                    } else { //GameOp.Operation.REMOVE
                        if (mov is Falcon) {
                            CommandCenter.initFalconAndDecrementFalconNum()
                        } else {
                            CommandCenter.movFriends.remove(mov)
                        }
                    }

                    Team.FLOATER -> if (action == GameOp.Action.ADD) {
                        CommandCenter.movFloaters.add(mov)
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.movFloaters.remove(mov)
                    }

                    Team.DEBRIS -> if (action == GameOp.Action.ADD) {
                        CommandCenter.movDebris.add(mov)
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.movDebris.remove(mov)
                    }
                }
            }
        }
    }

    //shows how to add walls or rectangular elements one brick at a time
    private fun buildWall() {
        val BRICK_SIZE = DIM.width / 30
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
    private fun checkFloaters() {
        spawnNewWallFloater()
        spawnShieldFloater()
        spawnNukeFloater()
    }

    private fun spawnNewWallFloater() {
        if (CommandCenter.frame % NewWallFloater.SPAWN_NEW_WALL_FLOATER == 0L && isBrickFree()) {
            CommandCenter.opsQueue.enqueue(NewWallFloater(), GameOp.Action.ADD)
        }
    }

    private fun spawnShieldFloater() {
        if (CommandCenter.frame % ShieldFloater.SPAWN_SHIELD_FLOATER == 0L) {
            CommandCenter.opsQueue.enqueue(ShieldFloater(), GameOp.Action.ADD)
        }
    }

    private fun spawnNukeFloater() {

        if (CommandCenter.frame % NukeFloater.SPAWN_NUKE_FLOATER == 0L) {
            CommandCenter.opsQueue.enqueue(NukeFloater(), GameOp.Action.ADD)
        }
    }

    private fun isBrickFree(): Boolean {
        //if there are no more Bricks on the screen
        var brickFree = true
        for (movFoe in CommandCenter.movFoes) {
            if (movFoe is Brick) {
                brickFree = false
                break
            }
        }
        return brickFree
    }
    //this method spawns new Large (0) Asteroids
    private fun spawnBigAsteroids(nNum: Int) {
        var localNum = nNum
        while (localNum-- > 0) {
            //Asteroids with size of zero are big
            CommandCenter.opsQueue.enqueue(Asteroid(0), GameOp.Action.ADD)
        }
    }

    private fun spawnSmallerAsteroidsOrDebris(originalAsteroid: Asteroid) {
        var size = originalAsteroid.size
        //small asteroids
        if (size > 1) {
            CommandCenter.opsQueue.enqueue(WhiteCloudDebris(originalAsteroid), GameOp.Action.ADD)
        } else {
            //for large (0) and medium (1) sized Asteroids only, spawn 2 or 3 smaller asteroids respectively
            //We can use the existing variable (size) to do this
            size += 2
            while (size-- > 0) {
                CommandCenter.opsQueue.enqueue(Asteroid(originalAsteroid), GameOp.Action.ADD)
            }
        }
    }

    //if there are no more Asteroids on the screen
    fun isLevelClear(): Boolean {
            //if there are no more Asteroids on the screen
            var asteroidFree = true
            for (movFoe in CommandCenter.movFoes) {
                if (movFoe is Asteroid) {
                    asteroidFree = false
                    break
                }
            }
            return asteroidFree
    }

    private fun checkNewLevel() {
        if (isLevelClear()) {

            //currentLevel will be zero at beginning of game
            var level: Int = CommandCenter.level
            //award some points for having cleared the previous level
            CommandCenter.score = CommandCenter.score + (level * 10_000L)
            //more asteroids at each level to increase difficulty
            level = level + 1
            CommandCenter.level = level
            spawnBigAsteroids(level)
            CommandCenter.falcon.shield = Falcon.SPAWN_INIT_VALUE
            CommandCenter.falcon.showLevel = Falcon.SPAWN_INIT_VALUE

        }
    }

    // ===============================================
    // KEYLISTENER METHODS
    // ===============================================
    override fun keyPressed(e: KeyEvent) {
        val fal = CommandCenter.falcon
        val nKey = e.keyCode
        if (nKey == START && CommandCenter.isGameOver()) CommandCenter.initGame()
        when (nKey) {
            PAUSE -> {
                CommandCenter.paused = !CommandCenter.paused
                if (CommandCenter.paused) stopLoopingSounds(clpMusicBackground, clpThrust)
            }

            QUIT -> System.exit(0)
            UP -> {
                fal.thrusting = true
                if (!CommandCenter.paused && !CommandCenter.isGameOver()) clpThrust.loop(Clip.LOOP_CONTINUOUSLY)
            }

            LEFT -> fal.turnState = Falcon.TurnState.LEFT
            RIGHT -> fal.turnState = Falcon.TurnState.RIGHT

            else -> {}
        }
    }

    override fun keyReleased(e: KeyEvent) {
        val fal = CommandCenter.falcon
        val nKey = e.keyCode
        //show the key-code in the console
        //println(nKey)
        when (nKey) {
            FIRE -> {
                CommandCenter.opsQueue.enqueue(Bullet(fal), GameOp.Action.ADD)
                Sound.playSound("thump.wav")
            }

            LEFT, RIGHT -> fal.turnState = Falcon.TurnState.IDLE

            UP -> {
                fal.thrusting = false
                clpThrust.stop()
            }

            NUKE -> {
                if (CommandCenter.falcon.nukeMeter > 0){
                    CommandCenter.opsQueue.enqueue(Nuke(fal), GameOp.Action.ADD)
                    Sound.playSound("nuke.wav")
                    CommandCenter.falcon.nukeMeter = 0
                }
            }

            MUTE -> {
                CommandCenter.muted = !CommandCenter.muted
                if (!CommandCenter.muted) {
                    stopLoopingSounds(clpMusicBackground)
                } else {
                    clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY)
                }

            }
            else -> {}
        }
    }

    // does nothing, but we need it b/c of KeyListener contract
    override fun keyTyped(e: KeyEvent) {}

    //utility method for stop looping sounds.
    private fun stopLoopingSounds(vararg clpClips: Clip) {
        for (clp in clpClips) {
            clp.stop()
        }
    }


}
