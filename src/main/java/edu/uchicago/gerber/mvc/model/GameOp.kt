package edu.uchicago.gerber.mvc.model

/**
 * Created by ag on 6/17/2015.
 */

data class GameOp(
    val movable: Movable,
    val action: Action
){
    //this could also be a boolean, but we want to be explicit about what we're doing
    enum class Action {
        ADD, REMOVE
    }
}
