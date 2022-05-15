package ceribe.distributed_monitor

import org.zeromq.SocketType
import org.zeromq.ZMQ
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.system.exitProcess

/**
 * [canBeProcessed] - predicate that determines whether current state can be processed.
 *
 * [index] - index the process which uses this monitor.
 *
 * [addresses] - list of addresses of all processes.
 *
 * [startDelay] - amount of milliseconds that monitor will wait before executing the first task.
 *
 * [finishTimeout] - amount of milliseconds that monitor will wait for someone to request the token before dying.
 */
class DistributedMonitor<T>(
    constructor: () -> T,
    private val canBeProcessed: (T) -> Boolean,
    private val index: Int,
    addresses: List<String>,
    private val startDelay: Long = 5000,
    private val finishTimeout: Long = 2000
) where T : SerializableState {
    private val state: T = constructor()
    private var token: Token? = null
    private val numberOfProcesses = addresses.size
    private val rn: MutableList<Int>

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private val pubSocket: ZMQ.Socket
    private val subSocket: ZMQ.Socket

    init {
        if (index == 0) {
            token = Token(mutableListOf(), MutableList(numberOfProcesses) { 0 })
        }
        rn = MutableList(numberOfProcesses) { 0 }

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
            while (true) {
                val message = subSocket.recv()
                val firstInt = message.getInt(0)
                if (firstInt == 0) {
                    // Request Message
                    val senderNumber = message.getInt(1)
                    val requestNumber = message.getInt(2)
                    rn[senderNumber - 1] = maxOf(rn[senderNumber - 1], requestNumber)
                } else if (firstInt - 1 == index) {
                    // Token Message
                    lock.withLock {
                        processTokenMessage(message)
                        condition.signalAll()
                    }
                }
            }
        }

        // Give other processes time to start
        Thread.sleep(startDelay)
    }

    fun execute(block: T.() -> Unit) {
        var processed = false
        while (!processed) {
            if (token == null) {
                //Send request message to all processes
                rn[index]++
                pubSocket.send(composeRequestMessage())
            }
            lock.withLock {
                while (token == null) {
                    condition.await()
                }
                if (canBeProcessed(state)) {
                    state.block()
                    processed = true
                }
                updateQueueAndTryToSendToken()
            }
        }
    }

    private fun updateQueueAndTryToSendToken() {
        token!!.ln[index] = rn[index]
        for (i in 0 until numberOfProcesses) {
            val isIndexInQueue = token!!.queue.contains(i)
            val isWaitingForCriticalSection = token!!.ln[i] + 1 == rn[i]
            if (!isIndexInQueue && isWaitingForCriticalSection) {
                token!!.queue.add(i)
            }
        }
        val nodeNumber = token!!.queue.removeFirstOrNull()
        if (nodeNumber != null) {
            pubSocket.send(composeTokenMessage(nodeNumber + 1))
            token = null
        }
    }

    // TODO maybe pack it into some lambda so API is cleaner
    /**
     * Waits until someone requests the token and sends it. After timeout dies forcibly.
     */
    fun die() {
        thread(start = true) {
            while (token != null) {
                updateQueueAndTryToSendToken()
                Thread.sleep(50)
            }
        }
        if (token != null) {
            Thread.sleep(finishTimeout)
        }
        exitProcess(0)
    }

    private fun composeRequestMessage(): ByteArray {
        return listOf(0, index + 1, rn[index]).toByteArray()
    }

    /**
     *  Returns a ByteArray containing processNumber, serialized token and serialized state.
     */
    private fun composeTokenMessage(processNumber: Int): ByteArray {
        token?.let {
            return processNumber.toByteArray() + it.serialize() + state.serialize()
        }
        throw NullPointerException("Token is null")
    }

    /**
     * Reads given [message] and updates token and state.
     */
    private fun processTokenMessage(message: ByteArray) {
        token = Token()

        val messageWithoutProcessNumber = message.copyOfRange(4, message.size)
        val offset = token!!.deserialize(messageWithoutProcessNumber, numberOfProcesses)

        val stateBytes = message.sliceArray(4 + offset until message.size)
        state.deserialize(stateBytes)
    }
}