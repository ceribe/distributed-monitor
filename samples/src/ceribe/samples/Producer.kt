package ceribe.samples

import ceribe.distributed_monitor.DistributedMonitor

fun main(args: Array<String>) {
    val monitor = DistributedMonitor(
        ::IntList,
        canBeProcessed = { it.values.size < 5 },
        index = 0,
        addresses = addresses
    )

    (1..200).forEach {
        monitor.execute {
            values.add(it)
            println("Produced value: $it")
        }
    }

    monitor.die()
}
