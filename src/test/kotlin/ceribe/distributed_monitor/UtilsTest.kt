package ceribe.distributed_monitor

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class UtilsTest {

    @Test
    fun `should get the same after converting list twice`() {
        val intList = listOf(1, 2, 3, 4, 5)
        val intListConvertedToByteArray = intList.toByteArray()
        val twiceConvertedIntList = intListConvertedToByteArray.toList()
        assertEquals(5, twiceConvertedIntList.size)
        assertIterableEquals(intList, twiceConvertedIntList)
    }

    @Test
    fun `should get proper int`() {
        val intList = listOf(1, 2, 3, 4, 5)
        val intListConvertedToByteArray = intList.toByteArray()
        with (intListConvertedToByteArray) {
            assertEquals(1, getInt(0))
            assertEquals(2, getInt(1))
            assertEquals(3, getInt(2))
            assertEquals(4, getInt(3))
            assertEquals(5, getInt(4))
        }
    }

    @Test
    fun `should get the same after converting int twice`() {
        val int = 45623
        val intConvertedToByteArray = int.toByteArray()
        val twiceConvertedInt = intConvertedToByteArray.getInt(0)
        assertEquals(int, twiceConvertedInt)
    }
}