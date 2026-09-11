import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("java-library")
    id("com.diffplug.spotless")
    id("io.papermc.paperweight.userdev")
    id("net.ltgt.errorprone")
    id("xyz.jpenilla.run-paper")
}

val minecraftVersion = libs.versions.minecraft.get()
val apiVersion = minecraftVersion.split('.').take(2).joinToString(".")

base {
    archivesName = "structure-bounds"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    // Paper
    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")

    // Annotations
    compileOnly(libs.errorprone.annotations)
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)

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
