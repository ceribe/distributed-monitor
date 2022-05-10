package ceribe.samples

import ceribe.distributed_monitor.DistributedMonitor
import ceribe.distributed_monitor.State

class IntListState : State() {
    val values = mutableListOf<Int>()
    override fun serialize(): String {
        TODO("Not yet implemented")
    }

    override fun deserialize(data: String) {
        TODO("Not yet implemented")
    }
}

fun main(args: Array<String>) {
    val state = IntListState()
    val monitor = DistributedMonitor(state)

    (1..100).forEach {
        monitor.execute {
            values.add(it)
            println("Added $it")
        }
    }
}
