import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.bolsillo.feature.record"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":designsystem"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Shared i18n → Android strings.xml (Article VI/VII).
// `./gradlew :feature-record:generateI18nStrings` rewrites
// res/values/strings.xml (es default) + res/values-en/strings.xml (en)
// from shared-assets/i18n/{es,en}.json. The dotted shared keys map to
// underscore Android resource names (e.g. record.save → record_save).
// The shared JSON is the single source — do not hand-edit the XML.
abstract class GenerateI18nStringsTask : DefaultTask() {
    @get:InputFile abstract val esJson: RegularFileProperty

    @get:InputFile abstract val enJson: RegularFileProperty

    @get:OutputDirectory abstract val resDir: DirectoryProperty

    @TaskAction
    fun run() {
        writeFor(esJson.get().asFile, resDir.get().dir("values").asFile)
        writeFor(enJson.get().asFile, resDir.get().dir("values-en").asFile)
    }

    private fun writeFor(
        src: File,
        destDir: File,
    ) {
        destDir.mkdirs()
        val text = src.readText()
        val regex = Regex("\"([a-zA-Z0-9_.]+)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
        val entries = mutableListOf<Pair<String, String>>()
        var inStrings = false
        for (line in text.lineSequence()) {
            if (!inStrings && line.contains("\"strings\"")) {
                inStrings = true
                continue
            }
            if (!inStrings) continue
            regex.findAll(line).forEach { m ->
                val key = m.groupValues[1]
                if (key == "locale" || key == "default") return@forEach
                val resName = key.replace('.', '_')
                val raw = m.groupValues[2]
                val xml =
                    raw
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("'", "\\'")
                        .replace("\"", "\\\"")
                entries += resName to xml
            }
        }
        val out = StringBuilder()
        out.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        out.appendLine("<!-- GENERATED from shared-assets/i18n/" + src.name + ". Do not edit. -->")
        out.appendLine("<resources>")
        entries.forEach { (k, v) -> out.appendLine("    <string name=\"$k\">$v</string>") }
        out.appendLine("</resources>")
        File(destDir, "strings.xml").writeText(out.toString())
    }
}

tasks.register<GenerateI18nStringsTask>("generateI18nStrings") {
    esJson.set(rootProject.file("../shared-assets/i18n/es.json"))
    enJson.set(rootProject.file("../shared-assets/i18n/en.json"))
    resDir.set(layout.projectDirectory.dir("src/main/res"))
}
