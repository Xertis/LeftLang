package builder

import java.io.File
import java.nio.file.Files

import scripts.utils.Logger
import scripts.utils.LogLevel

import generator.Generator
import lexer.Lexer
import parser.Parser

import scripts.utils.Toml
import scripts.utils.OsCmd

object Manager {

    val logger = Logger.getLogger("Left-BuildSystem")
    var leftConfig: com.moandjiezana.toml.Toml? = null

    init {
        Logger.setGlobalLevel(LogLevel.DEBUG)
        Logger.setGlobalColors(true)
        Logger.setGlobalTime(true)
        Logger.setGlobalTimeFormat("HH:mm:ss")

        val leftConfigFile = File("left.toml")
        if (leftConfigFile.exists()) leftConfig = Toml.parseFromPath("left.toml")
    }

    fun translate(source: String): String {
        val lexer = Lexer(source = " $source ")

        lexer.toTokens()

        val parser = Parser(lexer.tokens)
        val program = parser.makeAst()
        val generator = Generator(program)

        return generator.startGen()
    }

    private fun processTranslatedDirectory(sourceDir: File, targetDir: File) {
        sourceDir.listFiles()?.forEach { file ->
            val relativePath = sourceDir.toPath().relativize(file.toPath())
            val targetFile = targetDir.toPath().resolve(relativePath).toFile()

            when {
                file.isDirectory -> {
                    targetFile.mkdirs()
                    processTranslatedDirectory(file, targetFile)
                }
                file.isFile -> {
                    when(file.extension) {
                        "l" -> {
                            logger.info("The \"${file.name}\" file is being translated...", 2)

                            val C99Code = translate(file.readText())
                            val newTargetFile = File(targetFile.parent, "${file.nameWithoutExtension}.c")

                            newTargetFile.writeText(C99Code)
                        }
                        else -> {
                            file.copyTo(targetFile, overwrite = true)
                        }
                    }
                }
            }
        }
    }

    private fun buildProject(buildDir: File, translatedDir: File) {
        val osName = System.getProperty("os.name").lowercase().replace(" ", "-")

        logger.info("reading \"left.toml\"...", 2)

        val project = leftConfig!!.getTable("project")
        val build = leftConfig!!.getTable("build")

        val mainFile = project.getString("main")
        val buildCommand = build.getString(osName)

        when {
            mainFile == null -> throw RuntimeException("The main file was not found")
            buildCommand == null -> throw RuntimeException("No build command found for operating system \"$osName\"")
        }

        val translations = hashMapOf(
            "MainFile" to mainFile,
            "Os" to osName,
            "TranslatedDir" to translatedDir.path,
            "BuildDir" to buildDir.path
        )

        val command = OsCmd.prepareCommand(buildCommand, translations)

        logger.info("Executing the command: ${command.joinToString(" ")}", 2)
        OsCmd.run(command)
    }

    fun translateFolder() {
        logger.info("Starting of project translation")
        val srcDir = File("src")
        val translatedDir = File("target/translated")

        if (!srcDir.exists()) {
            logger.error("Folder \"src\" not found", 2)
            return
        }

        if (translatedDir.exists()) {
            translatedDir.deleteRecursively()
            logger.info("The contents of the \"translated\" folder have been cleared", 2)
        }

        translatedDir.mkdirs()
        processTranslatedDirectory(srcDir, translatedDir)
        logger.info("Project translation completed successfully")
    }

    fun build() {
        try {
            logger.info("Starting of project build")
            val translatedDir = File("target/translated")
            val buildDir = File("target/build")
            val leftConfigFile = File("left.toml")

            if (!leftConfigFile.exists() && leftConfig != null) {
                logger.error("File \"left.toml\" not found", 2)
                return
            }

            if (!translatedDir.exists()) {
                logger.error("Folder \"translated\" not found", 2)
                return
            }

            if (buildDir.exists()) {
                buildDir.deleteRecursively()
                logger.info("The contents of the \"build\" folder have been cleared", 2)
            }

            buildDir.mkdirs()

            buildProject(buildDir, translatedDir)
        } catch (e: Exception) {
            logger.fatal("Left fatal error: ${e.message}", 2)
        }
    }

    fun create() {
        logger.info("Creating the project structure...")
        val configPath = "left.toml"

        try {
            val currentDir = File(System.getProperty("user.dir"))
            val targetFile = File(currentDir, configPath)
            val srcDir = File(currentDir, "src")

            // left.toml
            if (!targetFile.exists()) {
                logger.info("Create \"left.toml\"", 2)
                val inputStream = object {}.javaClass.getResourceAsStream("/$configPath")
                    ?: error("File $configPath not found in resources")

                inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                logger.info("\"left.toml\" already exists (skipped creation)", 2)
            }

            // src
            if (!srcDir.exists()) {
                logger.info("Creating the \"src\" directory", 2)
                Files.createDirectories(srcDir.toPath())
            } else {
                logger.info("\"src\" directory already exists (skipped creation)", 2)
            }

        } catch (e: Exception) {
            logger.fatal("Left fatal error: ${e.message}", 2)
        }

        logger.info("The project structure has been successfully created.")
    }
}