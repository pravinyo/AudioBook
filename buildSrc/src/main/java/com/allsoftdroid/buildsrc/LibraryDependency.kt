package com.allsoftdroid.buildsrc

import CoreVersion

private object LibraryVersion {
    const val MOSHI = "1.9.2"
    const val DROPBOX_STORE = "4.0.0-alpha03"
    const val JSOUP = "1.12.1"
    const val SUPPORT_V13 = "28.0.0"
    const val MULTISTAGE_TOGGLE_BTN = "0.2.2"
    const val LEAKY_CANARY = "2.0"
    const val EXOPLAYER = "2.10.8"
    const val RX_RELAY = "2.1.1"
    const val RX_JAVA = "2.2.10"
    const val RX_KOTLIN = "2.4.0"
    const val RX_ANDROID = "2.1.1"
    const val CARD_VIEW = "28.0.0"
    const val GLIDE = "4.9.0"
    const val ROOM = "2.2.0-rc01"
    const val version_retrofit_coroutines_adapter = "0.9.2"
    const val GSON = "2.8.5"
    const val koin_version = "2.1.0-alpha-3"
    const val RETROFIT = "2.6.1"
    const val LOGGING_INTERCEPTOR = "4.3.0"
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
    const val FRAGMENT_KTX = CoreVersion.FRAGMENT_KTX
    const val LIFECYCLE_VIEW_MODEL_KTX = "2.2.0-alpha01"
    const val COIL = "0.6.1"
    const val K_ANDROID = "0.8.8@aar"
    const val LOTTIE = "3.3.1"
}

object LibraryDependency {
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-stdlib:${CoreVersion.KOTLIN}"
    // Required by Android dynamic feature modules and SafeArgs
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect:${CoreVersion.KOTLIN}"

    const val KOIN = "org.koin:koin-android:${LibraryVersion.koin_version}"
    const val KOIN_SCOPE="org.koin:koin-android-scope:${LibraryVersion.koin_version}"
    const val KOIN_VIEWMODEL = "org.koin:koin-android-viewmodel:${LibraryVersion.koin_version}"

    // Koin AndroidX
    const val KOIN_X_SCOPE = "org.koin:koin-androidx-scope:${LibraryVersion.koin_version}"
    const val KOIN_X_VIEWMODEL = "org.koin:koin-androidx-viewmodel:${LibraryVersion.koin_version}"
    const val KOIN_X_FRAGMENT="org.koin:koin-androidx-fragment:${LibraryVersion.koin_version}"

    const val RETROFIT = "com.squareup.retrofit2:retrofit:${LibraryVersion.RETROFIT}"
    const val RETROFIT_MOSHI_CONVERTER = "com.squareup.retrofit2:converter-moshi:${LibraryVersion.RETROFIT}"
    const val RETROFIT_SCALARS = "com.squareup.retrofit2:converter-scalars:${LibraryVersion.RETROFIT}"
    const val RETROFIT_COROUTINES_ADAPTER="com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:${LibraryVersion.version_retrofit_coroutines_adapter}"

    const val GSON ="com.google.code.gson:gson:${LibraryVersion.GSON}"
    const val MOSHI = "com.squareup.moshi:moshi:${LibraryVersion.MOSHI}"
    const val MOSHI_KOTLIN_CODEGEN ="com.squareup.moshi:moshi-kotlin-codegen:${LibraryVersion.MOSHI}"

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
    const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${LibraryVersion.FRAGMENT_KTX}"

    const val LIFECYCLE_EXTENSIONS = "androidx.lifecycle:lifecycle-extensions:${LibraryVersion.LIFECYCLE_VIEW_MODEL_KTX}"
    const val LIFECYCLE_COMPILER ="androidx.lifecycle:lifecycle-compiler:${LibraryVersion.LIFECYCLE_VIEW_MODEL_KTX}"
    const val LIFECYCLE_VIEW_MODEL_KTX =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${LibraryVersion.LIFECYCLE_VIEW_MODEL_KTX}"

    const val NAVIGATION_FRAGMENT_KTX = "androidx.navigation:navigation-fragment-ktx:${CoreVersion.NAVIGATION}"
    const val NAVIGATION_UI_KTX = "androidx.navigation:navigation-ui-ktx:${CoreVersion.NAVIGATION}"

    const val COIL = "io.coil-kt:coil:${LibraryVersion.COIL}"
    const val K_ANDROID = "com.pawegio.kandroid:kandroid:${LibraryVersion.K_ANDROID}"
    const val LOTTIE = "com.airbnb.android:lottie:${LibraryVersion.LOTTIE}"


    const val ROOM_RUNTIME = "androidx.room:room-runtime:${LibraryVersion.ROOM}"
    const val ROOM_KTX = "androidx.room:room-ktx:${LibraryVersion.ROOM}"
    const val ROOM_COMPILER = "androidx.room:room-compiler:${LibraryVersion.ROOM}"

    const val GLIDE = "com.github.bumptech.glide:glide:${LibraryVersion.GLIDE}"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:${LibraryVersion.GLIDE}"

    const val CARD_VIEW = "com.android.support:cardview-v7:${LibraryVersion.CARD_VIEW}"

    const val RX_JAVA = "io.reactivex.rxjava2:rxjava:${LibraryVersion.RX_JAVA}"
    const val RX_KOTLIN ="io.reactivex.rxjava2:rxkotlin:${LibraryVersion.RX_KOTLIN}"
    const val RX_ANDROID = "io.reactivex.rxjava2:rxandroid:${LibraryVersion.RX_ANDROID}"
    const val RX_RELAY = "com.jakewharton.rxrelay2:rxrelay:${LibraryVersion.RX_RELAY}"

    const val EXOPLAYER = "com.google.android.exoplayer:exoplayer:${LibraryVersion.EXOPLAYER}"

    const val LEAKY_CANARY = "com.squareup.leakcanary:leakcanary-android:${LibraryVersion.LEAKY_CANARY}"

    const val MULTISTAGE_TOGGLE_BUTTON = "org.honorato.multistatetogglebutton:multistatetogglebutton:${LibraryVersion.MULTISTAGE_TOGGLE_BTN}"

    const val SUPPORT_V13 = "com.android.support:support-v13:${LibraryVersion.SUPPORT_V13}"

    const val JSOUP = "org.jsoup:jsoup:${LibraryVersion.JSOUP}"

    const val DROPBOX_STORE = "com.dropbox.mobile.store:store4:${LibraryVersion.DROPBOX_STORE}"
}
