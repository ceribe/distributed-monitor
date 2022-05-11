package ceribe.samples

import ceribe.distributed_monitor.DistributedMonitor

fun main(args: Array<String>) {
    val monitor = DistributedMonitor(::IntList)

    (1..100).forEach {
        monitor.execute {
            values.add(it)
        }
    }
}
