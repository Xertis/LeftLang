package scripts.utils

import console.Command
import java.io.BufferedReader
import java.io.InputStreamReader

object OsCmd {

    fun run(command: Array<String>): String {
        return try {
            val process = ProcessBuilder(*command)
                .redirectErrorStream(true)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            process.waitFor()
            output.trim()
        } catch (e: Exception) {
            "Ошибка выполнения команды: ${e.message}"
        }
    }

    fun prepareCommand(
        command: String,
        variables: HashMap<String, String>? = null
    ): Array<String> {
        var processedCommand = command
        variables?.forEach { (key, value) ->
            processedCommand = processedCommand.replace("{$key}", value)
        }

        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        return if (isWindows) {
            arrayOf("cmd", "/c", processedCommand)
        } else {
            arrayOf("bash", "-c", processedCommand)
        }
    }
}
