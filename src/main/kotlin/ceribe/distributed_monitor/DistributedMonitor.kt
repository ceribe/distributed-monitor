package ceribe.distributed_monitor

import org.zeromq.SocketType
import org.zeromq.ZMQ
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/**
 * @param index index of the process which uses this monitor
 * @param addresses list of addresses of all processes
 * @param startDelay amount of milliseconds that monitor will wait before executing the first task
 * @param finishTimeout amount of milliseconds that monitor will wait after [die] is called before dying
 */
class DistributedMonitor<T>(
    constructor: () -> T,
    private val index: Int,
    addresses: List<String>,
    private val startDelay: Long = 5000,
    private val finishTimeout: Long = 2000
) where T : SerializableState {
    private val state: T = constructor()

    private val numberOfProcesses = addresses.size

    private var token: Token? = null
    private val rn: MutableList<Int>

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    private val pubSocket: ZMQ.Socket
    private val subSocket: ZMQ.Socket

    private lateinit var communicationThread: Thread

    init {
        // Init Suzuki-Kasami algorithm
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

        startCommunicationThread()

        // Give other processes time to start
        Thread.sleep(startDelay)
    }

    private fun startCommunicationThread() {
        communicationThread = thread(start = true) {
            while (true) {
                val message = subSocket.recv()
                val firstInt = message.getInt(0)
                if (firstInt == 0) {
                    processRequestMessage(message)
                } else if (firstInt - 1 == index) {
                    lock.withLock {
                        processTokenMessage(message)
                        condition.signalAll()
                    }
                }
            }
        }
    }

    /**
     * Tries to execute given [task].
     * If [canTaskBeExecuted] returns true, then [task] is executed.
     * Otherwise, gives token up and requests it again.
     * Will try until it succeeds. After finishing [task] token will be sent to other process or
     * if queue is empty, token will stay in this monitor.
     */
    fun execute(canTaskBeExecuted: T.() -> Boolean = { true }, task: T.() -> Unit) {
        var executed = false
        while (!executed) {
            if (token == null) {
                rn[index]++
                pubSocket.send(composeRequestMessage())
            }
            lock.withLock {
                while (token == null) {
                    condition.await()
                }
                if (state.canTaskBeExecuted()) {
                    state.apply(task)
                    executed = true
                }
                updateQueueAndTryToSendToken()
            }
        }
    }

    /**
     * Adds all processes which requested token to queue and
     * sends it to first process in queue. If queue is empty,
     * token stays in this monitor. Should only be called
     * if this process holds token.
     */
    private fun updateQueueAndTryToSendToken() {
        token!!.ln[index] = rn[index]
        for (i in 0 until numberOfProcesses) {
            val isIndexInQueue = token!!.queue.contains(i+1)
            val isWaitingForCriticalSection = token!!.ln[i] + 1 == rn[i]
            if (!isIndexInQueue && isWaitingForCriticalSection) {
                token!!.queue.add(i+1)
            }
        }
        val processNumber = token!!.queue.removeFirstOrNull()
        if (processNumber != null) {
            pubSocket.send(composeTokenMessage(processNumber))
            token = null
        }
    }

    /**
     * Waits until someone requests the token and sends it. After timeout communication thread dies forcibly.
     * Monitor should not be used after calling this method.
     */
    fun die() {
        var remainingTimeout = finishTimeout
        while (token != null && remainingTimeout > 0) {
            updateQueueAndTryToSendToken()
            Thread.sleep(50)
            remainingTimeout -= 50
        }

        communicationThread.setUncaughtExceptionHandler { _: Thread, _: Throwable -> }
        communicationThread.interrupt()
    }

    /**
     * Returns a ByteArray containing 0, this process's number and its request number.
     */
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
     * Reads given [message] and updates rn list.
     */
    private fun processRequestMessage(message: ByteArray) {
        val senderNumber = message.getInt(1)
        val requestNumber = message.getInt(2)
        rn[senderNumber - 1] = maxOf(rn[senderNumber - 1], requestNumber)
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