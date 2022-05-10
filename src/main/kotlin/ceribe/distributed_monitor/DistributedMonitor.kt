package ceribe.distributed_monitor

class DistributedMonitor<T>(private val state: T) where T : State {

    fun execute(block: T.() -> Unit) {
        // TODO Get lock
        state.block()
        // TODO Release lock
    }
}