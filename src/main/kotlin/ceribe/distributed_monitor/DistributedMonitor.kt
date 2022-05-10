package ceribe.distributed_monitor

// TODO Add a list of ips and ports of other nodes
class DistributedMonitor<T>(private val state: T) where T : SerializableState {

    fun execute(block: T.() -> Unit) {
        // TODO Get lock and update state
        state.block()
        // TODO Release lock and send state to other nodes
    }
}