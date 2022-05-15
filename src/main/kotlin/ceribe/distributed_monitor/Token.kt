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
        var offset = 4

        queue = data.sliceArray( offset until offset + queueSize * 4).toList().toMutableList()
        offset += queueSize * 4

        ln = data.sliceArray(offset until offset + numberOfProcesses * 4).toList().toMutableList()
        offset += numberOfProcesses * 4

        return offset
    }
}