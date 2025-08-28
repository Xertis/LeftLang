package scripts.utils
import lexer.TokenizerStates
import lexer.LexerInterface

class Fsm(var lexer: LexerInterface) {
    var middlewares = mutableListOf<Pair<TokenizerStates, (LexerInterface) -> TokenizerStates?>>()
    var state = TokenizerStates.DEFAULT
    fun addMiddleware(state: TokenizerStates, middleware: (LexerInterface) -> TokenizerStates?) {
        middlewares.add(Pair(state, middleware))
    }

    fun process() {
        for (middleware in middlewares) {
            if (middleware.first == state) {
                val newState = middleware.second(lexer)

                if (newState == null) {
                    return
                }

                state = newState

                return
            }
        }
    }
}