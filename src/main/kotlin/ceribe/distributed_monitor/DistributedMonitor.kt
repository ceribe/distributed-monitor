package ceribe.distributed_monitor

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

// TODO Add a list of ips and ports of other nodes
class DistributedMonitor<T>(constructor: () -> T) where T : SerializableState {
    private val state: T = constructor()
    private var token: Token? = null
    private val rn: List<Int> = TODO()

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    init {
        // TODO Start communication thread which will listen to other nodes' publish sockets
        // TODO Possible messages are "request" and "token"
        // TODO Upon receiving a "request" message act accordingly to the algorithm
        // TODO Upon receiving a "token" message set the token to the received token and update state
    }

    fun execute(block: T.() -> Unit) {
        if (token == null ) {
            //TODO send request to other nodes
        }
        lock.withLock {
            while (token == null) {
                condition.await()
            }
            state.block()
            val nodeNumber = token!!.queue.poll()
            if (nodeNumber != null) {
                val messageToSend = serializeTokenMessage()
                // TODO Send token and state to node with nodeNumber
                token = null
            }
        }
    }

    // TODO maybe pack it into some lambda so API is cleaner
    // TODO add timeout to monitor's constructor and stop waiting after that time
    /**
     * Waits until someone will request the token and sends it.
     */
    fun die() {
        TODO("Not yet implemented")
    }


    // TODO maybe move it to Token class as "serializeWithState"
    /**
     *  Returns serialized token and state
     */
    private fun serializeTokenMessage(): ByteArray {
        TODO("Not yet implemented")
    }

    // TODO maybe move it to Token class as "deserializeWithState"
    /**
     * Returns deserialized token and updates monitor's state
     */
    private fun deserializeTokenMessage(message: ByteArray): Token {
        TODO("Not yet implemented")
    }
}