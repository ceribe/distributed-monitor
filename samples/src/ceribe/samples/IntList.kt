package ceribe.samples

import ceribe.distributed_monitor.SerializableState
import ceribe.distributed_monitor.toByteArray
import ceribe.distributed_monitor.toList

class IntList : SerializableState {
    val values = mutableListOf<Int>()

    override fun serialize(): ByteArray {
        return values.toList().toByteArray()
    }

    override fun deserialize(data: ByteArray) {
        val newValues = data.toList()
        values.clear()
        values.addAll(newValues)
    }
}