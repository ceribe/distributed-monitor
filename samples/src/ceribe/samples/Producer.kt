package ceribe.samples

import ceribe.distributed_monitor.DistributedMonitor
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    val monitor = DistributedMonitor(
        ::IntList,
        canBeProcessed = { it.values.size < 10 },
        index = 0,
        addresses = listOf("localhost:8001", "localhost:8002", "localhost:8003")
    )

    (1..200).forEach {
        monitor.execute {
            values.add(it)
            println("Produced value: $it")
        }
    }

    monitor.die()
}
