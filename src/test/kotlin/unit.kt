import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import console.Console


class LeftTests {
    private fun canTranslate(source: String): Boolean {
        return Console().process(arrayOf("translate", source, "-itContent"))
    }

    @Test
    fun valAndVar() {
        val status1 = canTranslate("""
            fun main() {
                val x: Heavy = ?
                x = 0
            }
            """
        )

        val status2 = canTranslate("""
            fun main() {
                var x: Heavy = ?
                x = 0
            }
            """
        )
        assertEquals(false, status1)
        assertEquals(true, status2)
    }
}