package ceribe.distributed_monitor

data class Token(var queue: MutableList<Int> = mutableListOf(), var ln: MutableList<Int> = mutableListOf()) {

    /**
     * Serializes the token into a byte array.
     * First 4 bytes are the size of the queue.
     * Next is the queue itself and then "ln" list.
     */
    fun serialize(): ByteArray {
        return queue.size.toByteArray() + queue.toByteArray() + ln.toByteArray()
    }

    /**
     * Deserializes the token from a [data] byte array.
     * [numberOfProcesses] is needed to know how long "ln" list is.
     * Returns number of read bytes.
     */
    fun deserialize(data: ByteArray, numberOfProcesses: Int): Int {
        val queueSize = data.getInt(0)
        queue = data.sliceArray(4 until 4 + queueSize).toList().toMutableList()
        ln = data.sliceArray(4 + queueSize until 4 + queueSize + numberOfProcesses).toList().toMutableList()
        return 4 + queueSize + numberOfProcesses
    }
}