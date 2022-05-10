package ceribe.distributed_monitor

interface SerializableState {
    fun serialize(): ByteArray
    fun deserialize(data: ByteArray)
}