package scripts.utils

class TokenBuffer(val ln: Int, val col: Int) {
    var buffer: String = ""
    var length: Int = 0

    fun append(char: Char) {
        buffer += char
        length++
    }

    fun popRight() {
        buffer = buffer.dropLast(1)
        length--
    }

    fun popLeft() {
        buffer = buffer.drop(1)
        length--
    }

    fun get(): String {
        return buffer
    }

    fun last(): Char {
        return buffer.last()
    }

    fun clear() {
        buffer = ""
        length = 0
    }
}