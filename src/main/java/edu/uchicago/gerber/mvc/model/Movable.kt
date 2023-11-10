package edu.uchicago.gerber.mvc.model

import java.awt.Graphics
import java.awt.Point
import java.util.*

interface Movable {
    enum class Team {
        FRIEND, FOE, FLOATER, DEBRIS
    }

    //for the game to move and draw movable objects
    fun move()
    fun draw(g: Graphics)

    //for collision detection. We can not use getCenter(), etc. b/c
    //that would conflict with Kotlin's automatic getters/setters; so we use myCenter(), etc.
    fun myCenter(): Point
    fun myRadius(): Int
    fun myTeam(): Team

    //callbacks which occur before or after this object is added or removed from the game-space.
    //this is your opportunity to add sounds or perform other side effects, before (add) or after (remove).
    fun add(list: MutableList<Movable>)
    fun remove(list: MutableList<Movable>)


} //end Movable
