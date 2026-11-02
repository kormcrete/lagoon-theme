import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "io.github.kormcrete"
version = "0.1.0-SNAPSHOT"

// LagoonTokens.kt is generated from tokens/lagoon.tokens.json, which the FlatLaf theme reads as well.
val tokensFile = rootProject.layout.projectDirectory.file("../tokens/lagoon.tokens.json")
val generatedTokensDir = layout.buildDirectory.dir("generated/lagoonTokens/commonMain/kotlin")

val generateLagoonTokens by tasks.registering {
    description = "Generates LagoonTokens.kt from tokens/lagoon.tokens.json."
    inputs.file(tokensFile)
    outputs.dir(generatedTokensDir)
    doLast {
        fun color(hex: Any?): String {
            val h = hex.toString().removePrefix("#").uppercase()
            require(h.length == 6 || h.length == 8) { "Not a colour: $hex" }
            return "Color(0x${if (h.length == 6) "FF$h" else h})"
        }
        fun size(value: Any?): String {
            val v = value.toString()
            return when {
                v.endsWith("dp") -> "${v.removeSuffix("dp")}.dp"
                v.endsWith("sp") -> "${v.removeSuffix("sp")}.sp"
                else -> error("A size needs dp or sp: $v")
            }
        }
        @Suppress("UNCHECKED_CAST")
        val json = JsonSlurper().parse(tokensFile.asFile) as Map<String, Any?>
        val identity = json["identity"] as Map<String, Any?>
        val colors = identity["color"] as Map<String, Any?>
        val tiles = identity["tiles"] as List<List<Any?>>
        val radius = identity["radius"] as Map<String, Any?>
        val metrics = json["metrics"] as Map<String, Map<String, Any?>>

        val out = StringBuilder()
        out.appendLine("// Generated from tokens/lagoon.tokens.json by :lagoon-compose:generateLagoonTokens. Do not edit.")
        out.appendLine("package io.github.kormcrete.lagoon.compose")
        out.appendLine()
        out.appendLine("import androidx.compose.ui.graphics.Color")
        out.appendLine("import androidx.compose.ui.unit.dp")
        out.appendLine("import androidx.compose.ui.unit.sp")
        out.appendLine()
        out.appendLine("internal object LagoonTokens {")
        out.appendLine("    object Colors {")
        colors.forEach { (name, value) -> out.appendLine("        val $name = ${color(value)}") }
        out.appendLine("    }")
        out.appendLine()
        out.appendLine("    val tiles = listOf(")
        tiles.forEach { pair -> out.appendLine("        ${color(pair[0])} to ${color(pair[1])},") }
        out.appendLine("    )")
        out.appendLine()
        out.appendLine("    object Radius {")
        radius.forEach { (name, value) -> out.appendLine("        val $name = ${size(value)}") }
        out.appendLine("    }")
        metrics.forEach { (platform, values) ->
            out.appendLine()
            out.appendLine("    val ${platform}Metrics = LagoonMetrics(")
            values.forEach { (name, value) -> out.appendLine("        $name = ${size(value)},") }
            out.appendLine("    )")
        }
        out.appendLine("}")

        val file = generatedTokensDir.get().file("io/github/kormcrete/lagoon/compose/LagoonTokens.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(out.toString())
    }
}

kotlin {
    jvmToolchain(17)

    androidTarget {
        publishLibraryVariants("release")
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            kotlin.srcDir(generateLagoonTokens)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                api(compose.material3)
                implementation(compose.components.resources)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "io.github.kormcrete.lagoon.compose.resources"
    generateResClass = always
}

android {
    namespace = "io.github.kormcrete.lagoon.compose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
