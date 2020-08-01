package com.allsoftdroid.buildsrc

object AndroidConfig {
    const val COMPILE_SDK_VERSION = 29
    const val MIN_SDK_VERSION = 21
    const val TARGET_SDK_VERSION = 29
    const val BUILD_TOOLS_VERSION = "29.0.0"

    const val versionMajor = 6
    const val versionMinor = 5
    const val versionPatch = 14
    var versionClassifier = ""
    const val isSnapshot = false

    val VERSION_CODE = CodeVersionGenerator.generateVersionCode()
    val VERSION_NAME = CodeVersionGenerator.generateVersionName()

    const val ID = "com.allsoftdroid.audiobook"
    const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
    const val SUPPORT_LIBRARY_VECTOR_DRAWABLES = true
}

interface BuildType {

    companion object {
        const val RELEASE = "release"
        const val DEBUG = "debug"
    }

    val isMinifyEnabled: Boolean
}

object BuildTypeDebug : BuildType {
    override val isMinifyEnabled = false
}

object BuildTypeRelease : BuildType {
    override val isMinifyEnabled = false
}

object TestOptions {
    const val IS_RETURN_DEFAULT_VALUES = true
}

object DataBinding{
    const val IS_ENABLED = true
}
