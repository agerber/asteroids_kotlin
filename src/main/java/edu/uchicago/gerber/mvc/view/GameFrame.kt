package edu.uchicago.gerber.mvc.view

import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JPanel

class GameFrame : JFrame() {
    private var contentPane: JPanel? = null
    private val borderLayout = BorderLayout()

    init {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK)
        try {
            initialize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Component initialization
    private fun initialize() {
        contentPane = getContentPane() as JPanel
        contentPane!!.layout = borderLayout
    }

    //Overridden so we can exit when window is closed
    override fun processWindowEvent(e: WindowEvent) {
        super.processWindowEvent(e)
        if (e.id == WindowEvent.WINDOW_CLOSING) {
            System.exit(0)
        }
    }
}
