package edu.uchicago.gerber.mvc.controller

import edu.uchicago.gerber.mvc.model.*
import edu.uchicago.gerber.mvc.model.Movable.Team
import edu.uchicago.gerber.mvc.view.GamePanel
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
            //this method will execute add() and remove() callbacks on Movable objects
            processGameOpsQueue()
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

        //This has order-of-growth of O(FOES * FRIENDS)
        for (movFriend in CommandCenter.movFriends) {
            for (movFoe in CommandCenter.movFoes) {
                pntFriendCenter = movFriend.myCenter()
                pntFoeCenter = movFoe.myCenter()
                radFriend = movFriend.myRadius()
                radFoe = movFoe.myRadius()

                //detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < radFriend + radFoe) {
                    //enqueue the friend
                    CommandCenter.opsQueue.enqueue(movFriend, GameOp.Action.REMOVE)
                    //enqueue the foe
                    CommandCenter.opsQueue.enqueue(movFoe, GameOp.Action.REMOVE)
                }
            } //end inner for
        } //end outer for

        //check for collisions between falcon and floaters. Order of growth of O(FLOATERS)
        val pntFalCenter = CommandCenter.falcon.myCenter()
        val radFalcon = CommandCenter.falcon.myRadius()
        var pntFloaterCenter: Point
        var radFloater: Int
        for (movFloater in CommandCenter.movFloaters) {
            pntFloaterCenter = movFloater.myCenter()
            radFloater = movFloater.myRadius()

            //detect collision
            if (pntFalCenter.distance(pntFloaterCenter) < (radFalcon + radFloater)) {
                //enqueue the floater
                CommandCenter.opsQueue.enqueue(movFloater, GameOp.Action.REMOVE)
            }
        }

    }//end meth

    private fun processGameOpsQueue() {

        //deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
        // mutating the movable linkedlists while iterating them above.
        while (!CommandCenter.opsQueue.isEmpty()) {
            val gameOp = CommandCenter.opsQueue.dequeue()


            //given team, determine which linked-list this object will be added-to or removed-from
            var list: MutableList<Movable>
            val mov = gameOp?.movable
            list = when (mov!!.myTeam()) {
                Team.FOE -> CommandCenter.movFoes
                Team.FRIEND -> CommandCenter.movFriends
                Team.FLOATER -> CommandCenter.movFloaters
                Team.DEBRIS -> CommandCenter.movDebris
                else -> CommandCenter.movDebris
            }

            //pass the appropriate linked-list from above
            //this block will execute the add() or remove() callbacks in the Movable models.
            val action = gameOp.action
            if (action === GameOp.Action.ADD)
                mov.addToGame(list)
            else //REMOVE
                mov.removeFromGame(list)
        } //end while
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
            if (CommandCenter.falcon.shield < Falcon.SPAWN_INIT_VALUE)
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
            FIRE -> CommandCenter.opsQueue.enqueue(Bullet(fal), GameOp.Action.ADD)
            LEFT, RIGHT -> fal.turnState = Falcon.TurnState.IDLE
            UP -> {
                fal.thrusting = false
                clpThrust.stop()
            }

            NUKE -> CommandCenter.opsQueue.enqueue(Nuke(fal), GameOp.Action.ADD)

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
