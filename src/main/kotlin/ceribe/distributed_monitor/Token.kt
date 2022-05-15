package ceribe.distributed_monitor

data class Token(var queue: MutableList<Byte>, var ln: List<Byte>)
