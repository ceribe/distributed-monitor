package ceribe.distributed_monitor

import org.zeromq.SocketType
import org.zeromq.ZMQ
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.system.exitProcess

class DistributedMonitor<T>(
    constructor: () -> T,
    private val canBeProcessed: (T) -> Boolean,
    index: Int,
    addresses: List<String>,
    private val timeout: Long = 2000
) where T : SerializableState {
    private val state: T = constructor()
    private var token: Token? = null
    private val numberOfProcesses = addresses.size
    private val rn: List<Int>

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private val pubSocket: ZMQ.Socket
    private val subSocket: ZMQ.Socket

    init {
        if (index == 0) {
            token = Token(mutableListOf(), List(numberOfProcesses) { 0 })
        }
        rn = List(numberOfProcesses) { 0 }

        // Init publishing socket
        val context = ZMQ.context(1)
        pubSocket = context.socket(SocketType.PUB)
        pubSocket.bind("tcp://${addresses[index]}")

        // Init subscribing socket
        subSocket = ZMQ.context(1).socket(SocketType.SUB)
        subSocket.subscribe("".toByteArray())
        val otherProcessesAddresses = addresses.filterIndexed { i, _ -> i != index }
        otherProcessesAddresses.forEach {
            subSocket.connect("tcp://$it")
        }

        thread(start = true) {
            val message = subSocket.recv()
            if (message[0] == 0.toByte()) {
                // Request Message
            } else {
                // Token Message
            }
        }

        // Give other processes time to start
        Thread.sleep(10000)
    }

    fun execute(block: T.() -> Unit) {
        var processed = false
        while (!processed) {
            if (token == null) {
                //TODO send request to other nodes
            }
            lock.withLock {
                while (token == null) {
                    condition.await()
                }
                if (canBeProcessed(state)) {
                    state.block()
                    processed = true
                }
                val nodeNumber = token!!.queue.removeFirstOrNull()
                if (nodeNumber != null) {
                    val messageToSend = serializeTokenMessage()
                    // TODO Send token and state to node with nodeNumber
                    token = null
                }
            }
        }
    }

    // TODO maybe pack it into some lambda so API is cleaner
    /**
     * Waits until someone requests the token and sends it. After timeout dies forcibly.
     */
    fun die() {
        Thread.sleep(timeout)
        exitProcess(0)
    }

    /**
     *  Returns serialized token and state
     */
    private fun serializeTokenMessage(): ByteArray {
        token?.let {
            return byteArrayOf(it.queue.size.toByte()) + it.queue.toByteArray() + it.ln.toByteArray() + state.serialize()
        }
        throw NullPointerException("Token is null")
    }

    /**
     * Returns deserialized token and updates monitor's state
     */
    private fun deserializeTokenMessage(message: ByteArray): Token {
        val queueSize = message[0].toInt()
        val queue = message.sliceArray(1 until 1 + queueSize)
        val ln = message.sliceArray(1 + queueSize until 1 + queueSize + numberOfProcesses)
        val stateBytes = message.sliceArray(1 + queueSize + numberOfProcesses until message.size)
        state.deserialize(stateBytes)
        return Token(queue.toMutableList(), ln.toList())
    }
}