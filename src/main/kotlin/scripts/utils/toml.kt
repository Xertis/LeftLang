package scripts.utils

import com.moandjiezana.toml.Toml
import java.io.File

object Toml {
    fun parseFromPath(path: String): Toml {
        val file = File(path)
        require(file.exists()) { "Файл TOML не найден по пути: $path" }
        return Toml().read(file)
    }

    fun parse(content: String): Toml {
        return Toml().read(content)
    }
}