package scripts.utils
import lexer.TokenizerStates

data class State(val state: TokenizerStates)

class Fsm {
    var middlewares = mutableListOf<(State) -> State?>()
    var state = State(TokenizerStates.DEFAULT)

    fun addMiddleware(state: TokenizerStates, middleware: (State) -> State?) {

    }
}