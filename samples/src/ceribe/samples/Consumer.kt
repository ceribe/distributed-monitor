package ceribe.samples

import ceribe.distributed_monitor.DistributedMonitor

fun main(args: Array<String>) {
    val monitor = DistributedMonitor(
        ::IntList,
        canBeProcessed = { it.values.isNotEmpty() },
        index = 1,
        addresses = listOf("localhost:8001", "localhost:8002", "localhost:8003")
    )

    repeat(100) {
        monitor.execute {
            if (values.isNotEmpty()) {
                val receivedValue = values.removeFirst()
                println("Received value: $receivedValue")
            }
        }
    }

    monitor.die()
}
