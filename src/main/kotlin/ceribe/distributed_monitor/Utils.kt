package ceribe.distributed_monitor

/**
 * Returns a byte array created from [this] int list. Each int is converted to 4 bytes.
 */
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

/**
 * Returns an int list created from [this] byte array. Each 4 bytes are converted to an int.
 */
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

/**
 * Returns int at [index] in [this] byte array.
 * For example when called with [index] == 1 bytes number 4,5,6,7 and used to create an int
 */
fun ByteArray.getInt(index: Int): Int {
    val b1 = this[index * 4].toInt() and 0xff
    val b2 = this[index * 4 + 1].toInt() and 0xff
    val b3 = this[index * 4 + 2].toInt() and 0xff
    val b4 = this[index * 4 + 3].toInt() and 0xff
    return b1 shl 24 or (b2 shl 16) or (b3 shl 8) or b4
}

/**
 * Converts [this] int to a byte array.
 */
fun Int.toByteArray(): ByteArray {
    val bytes = ByteArray(4)
    bytes[0] = (this shr 24).toByte()
    bytes[1] = (this shr 16).toByte()
    bytes[2] = (this shr 8).toByte()
    bytes[3] = this.toByte()
    return bytes
}

