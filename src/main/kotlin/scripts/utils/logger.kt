package scripts.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class LogLevel(val value: Int, val color: String) {
    TRACE(0, "\u001B[37m"),      // Белый
    DEBUG(1, "\u001B[36m"),      // Голубой
    INFO(2, "\u001B[32m"),       // Зеленый
    WARN(3, "\u001B[33m"),       // Желтый
    ERROR(4, "\u001B[31m"),      // Красный
    FATAL(5, "\u001B[35m");      // Пурпурный

    companion object {
        fun fromString(level: String): LogLevel {
            return entries.find { it.name.equals(level, true) } ?: INFO
        }
    }
}

class Logger private constructor(
    private val name: String,
    private var level: LogLevel,
    private var showColors: Boolean,
    private var showTime: Boolean,
    private var timeFormat: String
) {

    companion object {
        private var globalLevel: LogLevel = LogLevel.INFO
        private var globalShowColors: Boolean = true
        private var globalShowTime: Boolean = true
        private var globalTimeFormat: String = "HH:mm:ss.SSS"

        private val loggers = mutableMapOf<String, Logger>()
        private const val RESET = "\u001B[0m"

        fun getLogger(name: String): Logger {
            return loggers.getOrPut(name) {
                Logger(name, globalLevel, globalShowColors, globalShowTime, globalTimeFormat)
            }
        }

        fun setGlobalLevel(level: LogLevel) {
            globalLevel = level
            loggers.values.forEach { it.level = level }
        }

        fun setGlobalColors(enabled: Boolean) {
            globalShowColors = enabled
            loggers.values.forEach { it.showColors = enabled }
        }

        fun setGlobalTime(enabled: Boolean) {
            globalShowTime = enabled
            loggers.values.forEach { it.showTime = enabled }
        }

        fun setGlobalTimeFormat(format: String) {
            globalTimeFormat = format
            loggers.values.forEach { it.timeFormat = format }
        }
    }

    fun isEnabled(level: LogLevel): Boolean {
        return level.value >= this.level.value
    }

    fun trace(message: String, indent: Int = 0, throwable: Throwable? = null) = log(LogLevel.TRACE, message, indent, throwable)
    fun debug(message: String, indent: Int = 0, throwable: Throwable? = null) = log(LogLevel.DEBUG, message, indent, throwable)
    fun info(message: String, indent: Int = 0, throwable: Throwable? = null) = log(LogLevel.INFO, message, indent, throwable)
    fun warn(message: String, indent: Int = 0, throwable: Throwable? = null) = log(LogLevel.WARN, message, indent, throwable)
    fun error(message: String, indent: Int = 0, throwable: Throwable? = null) = log(LogLevel.ERROR, message, indent, throwable)
    fun fatal(message: String, indent: Int = 0, throwable: Throwable? = null) = log(LogLevel.FATAL, message, indent, throwable)

    inline fun <T> measureTime(name: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        debug("⏱️ Starting: $name")
        try {
            return block().also {
                val duration = System.currentTimeMillis() - start
                debug("✅ Completed: $name in ${duration}ms")
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - start
            error("❌ Failed: $name after ${duration}ms")
            throw e
        }
    }

    private fun log(level: LogLevel, message: String, indent: Int = 0, throwable: Throwable? = null) {
        if (!isEnabled(level)) return

        val indentation = " ".repeat(indent)
        val logEntry = buildLogEntry(level, "$indentation$message")
        println(logEntry)

        throwable?.let {
            println(buildLogEntry(level, "$indentation Exception: ${it.message}"))
            it.stackTrace.forEach { stackTraceElement ->
                println(buildLogEntry(level, "$indentation    at $stackTraceElement"))
            }
        }
    }

    private fun buildLogEntry(level: LogLevel, message: String): String {
        val builder = StringBuilder()

        if (showTime) {
            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern(timeFormat))
            builder.append("[$time] ")
        }

        if (showColors) {
            builder.append(level.color)
        }

        builder.append("${level.name} ")

        if (showColors) {
            builder.append(RESET)
        }

        builder.append("[$name] $message")

        return builder.toString()
    }
}

fun Logger.trace(throwable: Throwable, indent: Int = 0, message: () -> String) = trace(message(), indent, throwable)
fun Logger.debug(throwable: Throwable, indent: Int = 0, message: () -> String) = debug(message(), indent, throwable)
fun Logger.info(throwable: Throwable, indent: Int = 0, message: () -> String) = info(message(), indent, throwable)
fun Logger.warn(throwable: Throwable, indent: Int = 0, message: () -> String) = warn(message(), indent, throwable)
fun Logger.error(throwable: Throwable, indent: Int = 0, message: () -> String) = error(message(), indent, throwable)
fun Logger.fatal(throwable: Throwable, indent: Int = 0, message: () -> String) = fatal(message(), indent, throwable)

inline fun Logger.trace(indent: Int = 0, message: () -> String) = trace(message(), indent)
inline fun Logger.debug(indent: Int = 0, message: () -> String) = debug(message(), indent)
inline fun Logger.info(indent: Int = 0, message: () -> String) = info(message(), indent)
inline fun Logger.warn(indent: Int = 0, message: () -> String) = warn(message(), indent)
inline fun Logger.error(indent: Int = 0, message: () -> String) = error(message(), indent)
inline fun Logger.fatal(indent: Int = 0, message: () -> String) = fatal(message(), indent)
