package console

class Pipeline {
    var middlewares: MutableList<() -> String?> = mutableListOf()

    fun addMiddleware(middleware: () -> String?) {
        middlewares.add(middleware)
    }

    fun process(): MutableList<String> {
        val outputs: MutableList<String> = mutableListOf()
        for (middleware in middlewares) {
            var out: String? = null

            while (out == null) {
                out = middleware()
            }

            outputs.add(out)
        }

        return outputs
    }
}