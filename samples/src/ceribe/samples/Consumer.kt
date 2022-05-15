package ceribe.samples

import ceribe.distributed_monitor.DistributedMonitor

fun main(args: Array<String>) {
    val monitor = DistributedMonitor(
        ::IntList,
        canBeProcessed = { it.values.isNotEmpty() },
        index = 1,
        addresses = listOf("localhost:8001", "localhost:8002", "localhost:8003")
    )
    var sum = 0
    repeat(100) {
        monitor.execute {
            val receivedValue = values.removeFirst()
            sum += receivedValue
            println("Received value: $receivedValue")
        }
        Thread.sleep(50)
    }
    println("Sum: $sum")
    monitor.die()
}
