package edu.uchicago.gerber.mvc.view

import edu.uchicago.gerber.mvc.controller.CommandCenter
import edu.uchicago.gerber.mvc.controller.Game
import edu.uchicago.gerber.mvc.controller.Utils
import edu.uchicago.gerber.mvc.model.Movable
import edu.uchicago.gerber.mvc.model.prime.PolarPoint
import java.awt.*
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BiConsumer
import java.util.function.Function


class GamePanel(dim: Dimension) : Panel() {
    // ==============================================================
    // FIELDS 
    // ============================================================== 
    // The following "off" vars are used for the off-screen double-buffered image.
    private var imgOff: Image
    private var grpOff: Graphics
    private val gameFrame: GameFrame
    private val panelFont: Font
    private val panelFontBig: Font
    private var fontMetrics: FontMetrics

    private var fontWidth = 0
    private var fontHeight = 0

    private val decimalFormat = DecimalFormat("#,###")
    private val pntShipsRemianing: Array<Point>

    // ==============================================================
    // CONSTRUCTOR 
    // ==============================================================
    init {
        gameFrame = GameFrame()
        gameFrame.contentPane.add(this)

        // Robert Alef's awesome falcon design
        val listShip: MutableList<Point> = ArrayList()
        listShip.add(Point(0, 9))
        listShip.add(Point(-1, 6))
        listShip.add(Point(-1, 3))
        listShip.add(Point(-4, 1))
        listShip.add(Point(4, 1))
        listShip.add(Point(-4, 1))
        listShip.add(Point(-4, -2))
        listShip.add(Point(-1, -2))
        listShip.add(Point(-1, -9))
        listShip.add(Point(-1, -2))
        listShip.add(Point(-4, -2))
        listShip.add(Point(-10, -8))
        listShip.add(Point(-5, -9))
        listShip.add(Point(-7, -11))
        listShip.add(Point(-4, -11))
        listShip.add(Point(-2, -9))
        listShip.add(Point(-2, -10))
        listShip.add(Point(-1, -10))
        listShip.add(Point(-1, -9))
        listShip.add(Point(1, -9))
        listShip.add(Point(1, -10))
        listShip.add(Point(2, -10))
        listShip.add(Point(2, -9))
        listShip.add(Point(4, -11))
        listShip.add(Point(7, -11))
        listShip.add(Point(5, -9))
        listShip.add(Point(10, -8))
        listShip.add(Point(4, -2))
        listShip.add(Point(1, -2))
        listShip.add(Point(1, -9))
        listShip.add(Point(1, -2))
        listShip.add(Point(4, -2))
        listShip.add(Point(4, 1))
        listShip.add(Point(1, 3))
        listShip.add(Point(1, 6))
        listShip.add(Point(0, 9))

        //this just displays the ships remaining
        pntShipsRemianing  = Utils.pointsListToArray(listShip)


        gameFrame.pack()
        initView()
        gameFrame.size = dim
        gameFrame.title = "Game Base"
        gameFrame.isResizable = false
        gameFrame.isVisible = true
        isFocusable = true

        imgOff = createImage(Game.DIM.width, Game.DIM.height)
        grpOff = imgOff.getGraphics()
        panelFont = Font("SansSerif", Font.BOLD, 12)
        panelFontBig = Font("SansSerif", Font.BOLD + Font.ITALIC, 36)
        // val g = graphics // get the graphics context for the panel
        graphics.font = panelFont // take care of some simple font stuff
        fontMetrics = graphics.fontMetrics
    }

    // ==============================================================
    // METHODS 
    // ==============================================================


    //this is used for development, you can remove it from your final game
    private fun drawFrame(g: Graphics) {
        g.color = Color.white
        g.font = panelFont
        g.drawString("FRAME[KOTLIN]:" + CommandCenter.frame, fontWidth, Game.DIM.height - (fontHeight + 22))

    }

    override fun update(g: Graphics) {
        //create an image off-screen
        imgOff = createImage(Game.DIM.width, Game.DIM.height)
        //get its graphics context
        grpOff = imgOff.getGraphics()

        //Fill the off-screen image background with black.
        grpOff.setColor(Color.black)
        grpOff.fillRect(0, 0, Game.DIM.width, Game.DIM.height)
        //drawScore(grpOff)
        drawFrame(grpOff)
        if (CommandCenter.isGameOver()) {
            displayTextOnScreen(
                grpOff,
                "GAME OVER",
                "use the arrow keys to turn and thrust",
                "use the space bar to fire",
                "'S' to Start",
                "'P' to Pause",
                "'Q' to Quit",
                "'M' to toggle music",
                "'A' to toggle radar"
            )

        } else if (CommandCenter.paused) {
            val pausedString = "Game Paused"
            grpOff.drawString(
                pausedString,
                (Game.DIM.width - fontMetrics.stringWidth(pausedString)) / 2, Game.DIM.height / 4
            )
        }
        //game is playing
        else {

            processMovables(
                grpOff,
                CommandCenter.movDebris,
                CommandCenter.movFloaters,
                CommandCenter.movFoes,
                CommandCenter.movFriends
            )
            drawNumberShipsLeft(grpOff)
            drawMeters(grpOff)
            drawFalconStatus(grpOff);
           // drawScore(grpOff)
           // drawLevel(grpOff)


        }

        //after drawing all the movables or text on the offscreen-image, copy it in one fell-swoop to graphics context
        // of the game panel, and show it for ~40ms. If you attempt to draw sprites directly on the gamePanel, e.g.
        // without the use of a double-buffered off-screen image, you will see flickering.
        g.drawImage(imgOff, 0, 0, this)
    }

    private fun drawFalconStatus(g: Graphics){
        g.color = Color.white
        g.font = panelFont

        val OFFSET_LEFT = 220


        //draw the level upper-right corner
        val levelText = ("Level : [" + CommandCenter.level + "]  " +
                CommandCenter.universe.toString().replace('_', ' '))

        g.drawString(levelText.toString(), Game.DIM.width - OFFSET_LEFT, fontHeight) //upper-right corner

        g.drawString("Score : ${decimalFormat.format(CommandCenter.score)}" ,
            Game.DIM.width - OFFSET_LEFT,
            fontHeight * 2
        )



        val list = mutableListOf<String>()
        if (CommandCenter.falcon.showLevel > 0) list.add(levelText)
        if (CommandCenter.falcon.maxSpeedAttained) list.add("WARNING - SLOW DOWN")
        if (CommandCenter.falcon.nukeMeter > 0) list.add("PRESS F for NUKE")


        // if (list.size > 0) displayTextOnScreen(graphics, list.get(0))
        //use spread operator to convert to varargs
        if (list.size > 0) displayTextOnScreen(g, *list.toTypedArray())


    }

    //this method causes all sprites to move and draw themselves
    @SafeVarargs
    private fun processMovables(g: Graphics, vararg arrayOfListMovables: List<Movable>) {

        val moveDraw = BiConsumer { grp: Graphics, mov: Movable ->
            mov.move()
            mov.draw(grp)
        }

        //we use flatMap to flatten the List<Movable>[] passed-in above into a single stream of Movables
        Arrays.stream(arrayOfListMovables) //Stream<List<Movable>>
            .flatMap { obj: List<Movable> -> obj.stream() } //Stream<Movable>
            .forEach { m: Movable -> moveDraw.accept(g, m) }
    }

    private fun drawNumberShipsLeft(g: Graphics) {
        var numFalcons = CommandCenter.numFalcons
        while (numFalcons > 1) {
            drawOneShipLeft(g, numFalcons--)
        }
    }




    private fun drawMeters(g:Graphics){


        //will be a number between 0-100 inclusive
        val shieldValue = CommandCenter.falcon.shield / 2
        val nukeValue = CommandCenter.falcon.nukeMeter / 6
        drawOneMeter(g, Color.CYAN, 1, shieldValue)
        drawOneMeter(g, Color.YELLOW, 2, nukeValue)




    }


    private fun drawOneMeter(g: Graphics, color:Color, offSet: Int, percent: Int) {

        val xVal = Game.DIM.width - (100  + 120 * offSet)
        val yVal = Game.DIM.height - 45

        //draw meter
        g.color = color
        g.fillRect(xVal, yVal, percent, 10)

        //draw grey box
        g.color = Color.DARK_GRAY
        g.drawRect(xVal, yVal, 100, 10)
    }

    // Draw the number of falcons left on the bottom-right of the screen.
    private fun drawOneShipLeft(g: Graphics, offSet: Int) {

        g.color = Color.ORANGE

        val SIZE = 15
        val DEGREES = 90.0
        val X_POS = 27
        val Y_POS = 45

        val rotateFalcon90 = Function { pp: PolarPoint ->
            Point(
                (pp.r * SIZE
                        * Math.sin(
                    Math.toRadians(DEGREES)
                            + pp.theta
                )).toInt(),
                (pp.r * SIZE
                        * Math.cos(
                    Math.toRadians(DEGREES)
                            + pp.theta
                )).toInt()
            )
        }

        g.drawPolygon(
           Arrays.stream( Utils.cartesianToPolar(pntShipsRemianing))
                .map(rotateFalcon90)
                .map { pnt: Point -> pnt.x + Game.DIM.width - X_POS * offSet }
                .mapToInt { obj: Int -> obj }
                .toArray(),
            Arrays.stream( Utils.cartesianToPolar(pntShipsRemianing))
                .map(rotateFalcon90)
                .map { pnt: Point -> pnt.y + Game.DIM.height - Y_POS }
                .mapToInt { obj: Int -> obj }
                .toArray(),
            pntShipsRemianing.size)
    }

    private fun initView() {
        val g = graphics // get the graphics context for the panel
        g.font = panelFont // take care of some simple font stuff
        fontMetrics = g.fontMetrics
        fontWidth = fontMetrics.getMaxAdvance()
        fontHeight = fontMetrics.getHeight()
        g.font = panelFontBig // set font info
    }

    private  fun displayTextOnScreen(graphics: Graphics, vararg lines: String) {

        val spacer = AtomicInteger(0)
        val width = Game.DIM.width
        val height = Game.DIM.height
        lines
            .forEach { s: String ->
                graphics.drawString(
                    s,
                    (width - fontMetrics.stringWidth(s)) / 2,
                    ((height / 4) + fontHeight + spacer.getAndAdd(40))
                )
            }
    }




}
