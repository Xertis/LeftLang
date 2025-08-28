package scripts.utils
import lexer.TokenizerStates

data class State(val state: TokenizerStates)

class Fsm {
    var middlewares = mutableListOf<() -> Unit>()
    var state = State(TokenizerStates.DEFAULT)
}