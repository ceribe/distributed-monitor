package ceribe.samples

import ceribe.distributed_monitor.DistributedMonitor

fun main(args: Array<String>) {
    val state = IntList()
    val monitor = DistributedMonitor(state)

    (1..100).forEach {
        monitor.execute {
            values.add(it)
        }
    }
}
