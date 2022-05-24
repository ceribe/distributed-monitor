# Distributed Monitor

Distributed Monitor is a tool which makes it possible to execute atomic tasks in a distributed system while keeping a synchronized state shared between
all working processes. This implementation uses [Suzuki-Kasami Algorithm](https://www.geeksforgeeks.org/suzuki-kasami-algorithm-for-mutual-exclusion-in-distributed-system/) to achieve mutual exclusion and extends it to share some kind of state.

## How to use

### 1. Add distributed-monitor module to your project
Either copy the files or link the module.

### 2. Create a class which implements SerializableState interface

```kotlin
interface SerializableState {
    fun serialize(): ByteArray
    fun deserialize(data: ByteArray)
}
```

```kotlin
class SomeState : SerializableState {

    override fun serialize(): ByteArray {
        // Convert state to ByteArray
    }

    override fun deserialize(data: ByteArray) {
        // Reconstruct state from ByteArray
    }
}
```

### 3. Create monitor and pass all needed parameters

```kotlin
val monitor = DistributedMonitor(
  ::SomeState,  // Constructor of class which implements SerializableState interface
  canBeProcessed = { true }, // Lambda which tells monitor whether given state can be processed by this process
  index = 0, // This process's index
  addresses = listOf("localhost:8001", "localhost:8002", "localhost:8003") // Adresses of all processes which will work together
)
```

### 4. Implement program logic

```kotlin
monitor.execute {
  TODO() // Code to be executed inside critical section
}
```

You can think of "monitor.execute { ... }" as a distributed version of "synchronized(...) { ... }".

Additionaly "monitor.execute" brings given state class into scope.

### 5. After work with monitor is done clean up 

```kotlin
monitor.die()
```

It is important to call this function after finishing. Thank to this monitor will be able to send
the token to some process which requested it. This assumes that some other process will request token in at most
2000ms after this process finishes work. If 2000ms is not enough, pass a different "finishTimeout" to monitor's constructor.

### 6. Run

After implementing logic of your program(s) run them. Remember to start all processes in at most 5000ms after starting the first process
or else monitor won't work. If 5000ms is not enough, pass a different "startDelay" to monitor's constructor.

## Communication protocol

### Request Message
| Bytes  | Content                 |
| ------ | ----------------------- |
| 0 - 3  | Type - Broadcast        |
| 4 - 7  | Sender's process number |
| 8 - 11 | Sender's RN             |

### Token Message
| Bytes   | Content                          |
| ------- | -------------------------------- |
| 0 - 3   | Type / Recepient's proces number |
| 4 - 7   | Queue size                       |
| 8 - n   | Queue                            |
| n+1 - m | LN                               |
| m+1 - o | Serialized state                 |

Because process numbers start from 1 it is easy to tell which type of message just arrived.
Queue can have different size each time so the number of bytes needed for it are stored as "Queue size".
Size of "LN" is the number of processes. Serialized state's size should not matter for the protocol.

## Example program

Program listed below is the classic Producer/Consumer problem in which one process creates resources and other consumes them.

### State

```kotlin
class IntList : SerializableState {
    val values = mutableListOf<Int>()

    override fun serialize(): ByteArray {
        return values.toList().toByteArray()
    }

    override fun deserialize(data: ByteArray) {
        val newValues = data.toList()
        values.clear()
        values.addAll(newValues)
    }
}
```

"toByteArray" and "toList" used above are implemented be me in Utils.kt

### Producer

```kotlin
fun main(args: Array<String>) {
    val monitor = DistributedMonitor(
        ::IntList,
        canBeProcessed = { it.values.size < 5 },
        index = 0,
        addresses = listOf("localhost:8001", "localhost:8002")
    )

    (1..100).forEach {
        monitor.execute {
            values.add(it)
            println("Sent $it")
        }
    }

    monitor.die()
}
```

### Consumer

```kotlin
fun main(args: Array<String>) {
    val monitor = DistributedMonitor(
        ::IntList,
        canBeProcessed = { it.values.isNotEmpty() },
        index = 1,
        addresses = listOf("localhost:8001", "localhost:8002")
    )
    var sum = 0
    repeat(100) {
        monitor.execute {
            val receivedValue = values.removeFirst()
            sum += receivedValue
            println("Received $receivedValue")
        }
        Thread.sleep(50)
    }
    println("Sum: $sum")
    monitor.die()
}
```

### How to run

To run the example program open the project in Intellij IDEA, build all artifacts and run all of them (eg. one jar in each terminal)
