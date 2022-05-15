package ceribe.distributed_monitor

fun List<Int>.toByteArray(): ByteArray {
    val bytes = ByteArray(this.size * 4)
    this.forEachIndexed { index, i ->
        bytes[index * 4] = (i shr 24).toByte()
        bytes[index * 4 + 1] = (i shr 16).toByte()
        bytes[index * 4 + 2] = (i shr 8).toByte()
        bytes[index * 4 + 3] = i.toByte()
    }
    return bytes
}

fun ByteArray.toList(): List<Int> {
    val list = mutableListOf<Int>()
    for (i in 0 until this.size step 4) {
        val b1 = this[i].toInt() and 0xff
        val b2 = this[i + 1].toInt() and 0xff
        val b3 = this[i + 2].toInt() and 0xff
        val b4 = this[i + 3].toInt() and 0xff
        list.add(b1 shl 24 or (b2 shl 16) or (b3 shl 8) or b4)
    }
    return list
}

fun ByteArray.getInt(index: Int): Int {
    val b1 = this[index].toInt() and 0xff
    val b2 = this[index + 1].toInt() and 0xff
    val b3 = this[index + 2].toInt() and 0xff
    val b4 = this[index + 3].toInt() and 0xff
    return b1 shl 24 or (b2 shl 16) or (b3 shl 8) or b4
}

fun Int.toByteArray(): ByteArray {
    val bytes = ByteArray(4)
    bytes[0] = (this shr 24).toByte()
    bytes[1] = (this shr 16).toByte()
    bytes[2] = (this shr 8).toByte()
    bytes[3] = this.toByte()
    return bytes
}

