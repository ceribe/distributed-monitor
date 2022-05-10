class DistributedMonitor<T>(private val state: T) where T : State {

    fun execute(block: T.() -> Unit) {
        state.block()
    }
}