package edu.uchicago.gerber.mvc.model

import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class GameOpsQueue : LinkedList<GameOp>() {
    //this data structure is in contention by the "Event Dispatch" thread aka main-swing-thread, and the animation
    // thread. We must restrict access to it by one thread at a time by using a Lock.
    private val lock: Lock

    init {
        lock = ReentrantLock()
    }

    fun enqueue(mov: Movable, action: GameOp.Action) {
        try {
            lock.lock()
            addLast(GameOp(mov, action))
        } finally {
            lock.unlock()
        }
    }

    fun dequeue(): GameOp? {
        return try {
            lock.lock()
            removeFirst()
        } finally {
            lock.unlock()
        }
    }
}
