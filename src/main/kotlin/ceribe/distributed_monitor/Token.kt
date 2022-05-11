package ceribe.distributed_monitor

import java.util.Queue

data class Token(var queue: Queue<Byte>, var ln: List<Byte>)
