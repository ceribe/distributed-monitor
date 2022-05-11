package ceribe.samples

import ceribe.distributed_monitor.DistributedMonitor

fun main(args: Array<String>) {
    val monitor = DistributedMonitor(::IntList)

    repeat(100) {
        monitor.execute {
            if (values.isNotEmpty()) {
                val receivedValue = values.removeFirst()
                println("Received value: $receivedValue")
            }
        }
    }
}
