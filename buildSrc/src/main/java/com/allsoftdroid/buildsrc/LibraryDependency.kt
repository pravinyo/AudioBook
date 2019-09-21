package com.allsoftdroid.buildsrc

import CoreVersion

private object LibraryVersion {
    const val GLIDE = "4.9.0"
    const val ROOM = "2.1.0-rc01"
    const val version_retrofit_coroutines_adapter = "0.9.2"
    const val GSON = "2.8.5"
    const val KODEIN = "6.3.3"
    const val RETROFIT = "2.6.1"
    const val LOGGING_INTERCEPTOR = "4.1.0"
    const val STETHO = "1.5.0"
    const val TIMBER = "4.7.1"
    const val PLAY_CORE = "1.6.1"
    const val APP_COMPACT = "1.0.2"
    const val RECYCLER_VIEW = "1.1.0-beta04"
    const val COORDINATOR_LAYOUT = "1.0.0"
    // 1.1.x version is required in order to support the dark theme functionality in Android Q (adds Theme.MaterialComponents.DayNight)
    const val MATERIAL = "1.1.0-alpha09"
    const val CONSTRAINT_LAYOUT = "1.1.3"
    const val CORE_KTX = "1.0.2"
    const val FRAGMENT_KTX = "1.1.0-beta01"
    const val LIFECYCLE_VIEW_MODEL_KTX = "2.2.0-alpha01"
    const val COIL = "0.6.1"
    const val K_ANDROID = "0.8.8@aar"
    const val LOTTIE = "3.0.7"
}

object LibraryDependency {
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${CoreVersion.KOTLIN}"
    // Required by Android dynamic feature modules and SafeArgs
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect:${CoreVersion.KOTLIN}"
    const val KODEIN = "org.kodein.di:kodein-di-generic-jvm:${LibraryVersion.KODEIN}"
    const val KODEIN_ANDROID_X = "org.kodein.di:kodein-di-framework-android-x:${LibraryVersion.KODEIN}"

    const val RETROFIT = "com.squareup.retrofit2:retrofit:${LibraryVersion.RETROFIT}"
    const val RETROFIT_MOSHI_CONVERTER = "com.squareup.retrofit2:converter-moshi:${LibraryVersion.RETROFIT}"
    const val RETROFIT_SCALARS = "com.squareup.retrofit2:converter-scalars:${LibraryVersion.RETROFIT}"
    const val RETROFIT_COROUTINES_ADAPTER="com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:${LibraryVersion.version_retrofit_coroutines_adapter}"

    const val GSON ="com.google.code.gson:gson:${LibraryVersion.GSON}"

    const val LOGGING_INTERCEPTOR = "com.squareup.okhttp3:logging-interceptor:${LibraryVersion.LOGGING_INTERCEPTOR}"
    const val STETHO = "com.facebook.stetho:stetho:${LibraryVersion.STETHO}"
    const val STETHO_OK_HTTP = "com.facebook.stetho:stetho-okhttp3:${LibraryVersion.STETHO}"
    const val TIMBER = "com.jakewharton.timber:timber:${LibraryVersion.TIMBER}"
    const val SUPPORT_CONSTRAINT_LAYOUT =
        "androidx.constraintlayout:constraintlayout:${LibraryVersion.CONSTRAINT_LAYOUT}"
    const val PLAY_CORE = "com.google.android.play:core:${LibraryVersion.PLAY_CORE}"
    const val APP_COMPACT = "androidx.appcompat:appcompat:${LibraryVersion.APP_COMPACT}"
    const val RECYCLER_VIEW = "androidx.recyclerview:recyclerview:${LibraryVersion.RECYCLER_VIEW}"
    const val COORDINATOR_LAYOUT = "androidx.coordinatorlayout:coordinatorlayout:${LibraryVersion.COORDINATOR_LAYOUT}"
    const val MATERIAL = "com.google.android.material:material:${LibraryVersion.MATERIAL}"

    const val COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${CoreVersion.COROUTINES_ANDROID}"
    const val COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${CoreVersion.COROUTINES_ANDROID}"

    const val CORE_KTX = "androidx.core:core-ktx:${LibraryVersion.CORE_KTX}"
    const val FRAGMENT_KTX = "androidx.fragment:fragment:${LibraryVersion.FRAGMENT_KTX}"

    const val LIFECYCLE_EXTENSIONS = "androidx.lifecycle:lifecycle-extensions:${LibraryVersion.LIFECYCLE_VIEW_MODEL_KTX}"
    const val LIFECYCLE_COMPILER ="androidx.lifecycle:lifecycle-compiler:${LibraryVersion.LIFECYCLE_VIEW_MODEL_KTX}"
    const val LIFECYCLE_VIEW_MODEL_KTX =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${LibraryVersion.LIFECYCLE_VIEW_MODEL_KTX}"

    const val NAVIGATION_FRAGMENT_KTX = "androidx.navigation:navigation-fragment-ktx:${CoreVersion.NAVIGATION}"
    const val NAVIGATION_UI_KTX = "androidx.navigation:navigation-ui-ktx:${CoreVersion.NAVIGATION}"

    const val COIL = "io.coil-kt:coil:${LibraryVersion.COIL}"
    const val K_ANDROID = "com.pawegio.kandroid:kandroid:${LibraryVersion.K_ANDROID}"
    const val LOTTIE = "com.airbnb.android:lottie:${LibraryVersion.LOTTIE}"


    const val ROOM_KTX = "androidx.room:room-runtime:${LibraryVersion.ROOM}"
    const val ROOM_COMPILER = "androidx.room:room-compiler:${LibraryVersion.ROOM}"

    const val GLIDE = "com.github.bumptech.glide:glide:${LibraryVersion.GLIDE}"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:${LibraryVersion.GLIDE}"
}
