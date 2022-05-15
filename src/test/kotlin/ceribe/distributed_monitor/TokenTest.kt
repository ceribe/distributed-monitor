package ceribe.distributed_monitor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals

internal class TokenTest {
    @org.junit.jupiter.api.Test
    fun `should get the same after serializing and deserializing`() {
        val queue = mutableListOf(456456, 123164, 945615)
        val ln = mutableListOf(123, 456, 789, 123)
        val token = Token(queue, ln)
        val serializedToken = token.serialize()
        val token2 = Token()
        val offset = token2.deserialize(serializedToken, 4)
        assertIterableEquals(token.queue, token2.queue)
        assertIterableEquals(token.ln, token2.ln)
        assertEquals(4 + queue.size * 4 + ln.size * 4, offset)
    }
}