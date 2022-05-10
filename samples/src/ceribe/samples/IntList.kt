package ceribe.samples

import ceribe.distributed_monitor.SerializableState

class IntList : SerializableState {
    val values = mutableListOf<Int>()

    override fun serialize(): ByteArray {
        return values.joinToString(",").toByteArray()
    }

    override fun deserialize(data: ByteArray) {
        val decodedString = String(data)
        val newValues = decodedString.split(",").map { it.toInt() }
        values.clear()
        values.addAll(newValues)
    }
}