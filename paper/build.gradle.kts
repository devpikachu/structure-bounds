import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java-library")
    id("com.diffplug.spotless")
    id("io.papermc.paperweight.userdev")
    id("net.ltgt.errorprone")
    id("xyz.jpenilla.run-paper")
}

fun apiVersionFor(minecraft: String): String =
    if (minecraft.startsWith("1.")) minecraft.split('.').take(2).joinToString(".") else minecraft

val minecraftVersion = libs.versions.minecraft.get()
val apiVersion = apiVersionFor(minecraftVersion)

data class Backend(
    val minecraft: String,
    val release: Int,
    val symbols: Set<String>
)

val extraBackends = listOf(
    Backend("26.1.2", 25, setOf("MC_26")),
    Backend("26.2", 25, setOf("MC_26", "MC_26_2")),
)

abstract class PreprocessSources : DefaultTask() {

    @get:InputDirectory
    abstract val source: DirectoryProperty

    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @get:Input
    abstract val symbols: SetProperty<String>

    @TaskAction
    fun preprocess() {
        val defined = symbols.get()
        val from = source.get().asFile
        val into = destination.get().asFile

        into.deleteRecursively()

        from.walkTopDown().filter { it.isFile }.forEach { file ->
            val target = into.resolve(file.toRelativeString(from))
            target.parentFile.mkdirs()

            if (file.extension == "java") {
                target.writeText(rewrite(file.readLines(), defined).joinToString("\n", postfix = "\n"))
            } else {
                file.copyTo(target, overwrite = true)
            }
        }
    }

    private fun rewrite(lines: List<String>, defined: Set<String>): List<String> {
        val branch = ArrayDeque<Boolean>()

        val rewritten = lines.map { line ->
            val directive = directiveOf(line)

            when {
                directive != null && directive.startsWith(IF) -> {
                    branch.addLast(evaluate(directive.removePrefix(IF), defined))
                    line
                }
                directive == ELSE -> {
                    branch.addLast(!branch.removeLast())
                    line
                }
                directive == ENDIF -> {
                    branch.removeLast()
                    line
                }
                branch.isEmpty() -> line
                branch.all { it } -> uncomment(line)
                else -> comment(line)
            }
        }

        require(branch.isEmpty()) { "unbalanced //$IF in ${source.get().asFile}" }

        return rewritten
    }

    private fun directiveOf(line: String): String? {
        val token = line.trim()

        return if (token.startsWith("//")) token.removePrefix("//").trimStart() else null
    }

    private fun evaluate(expression: String, defined: Set<String>): Boolean {
        val token = expression.trim()

        return if (token.startsWith("!")) token.removePrefix("!").trim() !in defined else token in defined
    }

    private fun uncomment(line: String): String {
        val directive = directiveOf(line)

        if (directive == null || !directive.startsWith(HIDDEN)) {
            return line
        }

        return line.takeWhile { it == ' ' } + directive.removePrefix(HIDDEN).removePrefix(" ")
    }

    private fun comment(line: String): String {
        if (line.isBlank() || directiveOf(line)?.startsWith(HIDDEN) == true) {
            return line
        }

        val indent = line.takeWhile { it == ' ' }

        return indent + "// " + HIDDEN + " " + line.substring(indent.length)
    }

    private companion object {
        const val IF = "#if "
        const val ELSE = "#else"
        const val ENDIF = "#endif"
        const val HIDDEN = "\$\$"
    }
}

base {
    archivesName = "structure-bounds-$minecraftVersion"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

val backendShared = configurations.dependencyScope("backendShared").get()

configurations.compileOnly {
    extendsFrom(backendShared)
}

dependencies {
    // Paper
    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")

    // Annotations
    backendShared(libs.errorprone.annotations)
    backendShared(libs.jspecify)
    backendShared(libs.jetbrains.annotations)

    // Linting
    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
}

tasks {
    runServer {
        minecraftVersion(minecraftVersion)
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val props = mapOf(
            "version" to version,
            "apiVersion" to apiVersion,
            "minecraftVersion" to minecraftVersion
        )
        inputs.properties(props)
        filesMatching(listOf("plugin.yml", "structure-bounds.properties")) {
            expand(props)
        }
    }

    compileJava {
        options.release = 21
    }
}

extraBackends.forEach { backend ->
    val slug = backend.minecraft.replace('.', '_')

    val backendServer = configurations.dependencyScope("backend${slug}Server")

    val backendCompileClasspath = configurations.resolvable("backend${slug}CompileClasspath") {
        extendsFrom(backendShared, backendServer.get())
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>(Usage.JAVA_API))
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named<Category>(Category.LIBRARY))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named<LibraryElements>(LibraryElements.CLASSES))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named<Bundling>(Bundling.EXTERNAL))
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, backend.release)
        }
    }

    dependencies {
        add(backendServer.name, project(":paper-${backend.minecraft}"))
    }

    val preprocessBackend = tasks.register<PreprocessSources>("preprocessBackend${slug}Sources") {
        source = layout.projectDirectory.dir("src/main/java")
        destination = layout.buildDirectory.dir("generated/preprocessed/backend$slug")
        symbols = backend.symbols
    }

    val compileBackend = tasks.register<JavaCompile>("compileBackend${slug}Java") {
        source(preprocessBackend.flatMap { it.destination })
        classpath = files(backendCompileClasspath)
        destinationDirectory = layout.buildDirectory.dir("classes/java/backend$slug")
        options.release = backend.release

        options.annotationProcessorPath = sourceSets.main.get().annotationProcessorPath
        options.errorprone {
            enabled = true
        }
    }

    val processBackendResources = tasks.register<ProcessResources>("processBackend${slug}Resources") {
        val props = mapOf(
            "version" to version,
            "apiVersion" to apiVersionFor(backend.minecraft),
            "minecraftVersion" to backend.minecraft
        )
        inputs.properties(props)
        from(sourceSets.main.get().resources)
        into(layout.buildDirectory.dir("resources/backend$slug"))
        filesMatching(listOf("plugin.yml", "structure-bounds.properties")) {
            expand(props)
        }
    }

    val backendJar = tasks.register<Jar>("backend${slug}Jar") {
        archiveBaseName = "structure-bounds-${backend.minecraft}"

        from(compileBackend.flatMap { it.destinationDirectory })
        from(processBackendResources)
    }

    tasks.build {
        dependsOn(backendJar)
    }
}

spotless {
    java {
        target("src/main/java/**/*.java")

        palantirJavaFormat().formatJavadoc(true)
        removeUnusedImports()
        forbidWildcardImports()
        importOrder("", "javax|java", "\\#")
        formatAnnotations()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
    options.errorprone {
        disableWarningsInGeneratedCode = true

        error("NullAway")
        option("NullAway:OnlyNullMarked", "true")
    }
}
