// build.gradle.kts (Project)
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2") // Android Gradle Plugin
        classpath("com.google.gms:google-services:4.4.2") // Plugin Google Services
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory) // Gunakan layout.buildDirectory
}