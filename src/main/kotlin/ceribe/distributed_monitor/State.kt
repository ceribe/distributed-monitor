package ceribe.distributed_monitor

abstract class State {
    abstract fun serialize(): String
    abstract fun deserialize(data: String)
}