package com.allsoftdroid.buildsrc

object CodeVersionGenerator {


    fun generateVersionCode():Int {
        return AndroidConfig.MIN_SDK_VERSION * 10000000 + AndroidConfig.versionMajor * 10000 +
                AndroidConfig.versionMinor * 100 + AndroidConfig.versionPatch
    }

    fun generateVersionName():String {
        var versionName = "${AndroidConfig.versionMajor}.${AndroidConfig.versionMinor}.${AndroidConfig.versionPatch}"

        if (AndroidConfig.isSnapshot) {
            AndroidConfig.versionClassifier = "SNAPSHOT"
        }

        versionName += "-" + AndroidConfig.versionClassifier

        return versionName
    }
}