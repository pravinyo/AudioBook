private object TestLibraryVersion {
    const val DATA_BINDING_COMPILER = "3.6.3"
    const val HAMCREST = "2.2"
    const val MOCKITO_CORE = "2.10.0"
    const val FRAGMENT_TEST = CoreVersion.FRAGMENT_KTX
    const val JUNIT = "4.12"
    const val KLUENT = "1.53"
    const val TEST_RUNNER = "1.1.0"
    const val ESPRESSO_CORE = "3.1.0"
    const val MOCKITO = "3.0.0"
    const val MOCKITO_KOTLIN = "2.2.0"
    const val MOCKITO_DEXMAKER = "2.12.1"
    const val koin_version = "2.0.1"
    const val ANDROID_X_TEST = "1.2.0"

    const val ROBOELECTRIC_TEST = "4.3.1"
    const val EXT_KOTLIN_RUNNER = "1.1.1"
}

object TestLibraryDependency {
    const val JUNIT = "junit:junit:${TestLibraryVersion.JUNIT}"
    const val HAMCREST = "org.hamcrest:hamcrest:${TestLibraryVersion.HAMCREST}"

    const val KLUENT_ANDROID = "org.amshove.kluent:kluent-android:${TestLibraryVersion.KLUENT}"
    const val KLUENT = "org.amshove.kluent:kluent:${TestLibraryVersion.KLUENT}"
    const val TEST_RUNNER = "androidx.test:runner:${TestLibraryVersion.TEST_RUNNER}"

    const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:${TestLibraryVersion.ESPRESSO_CORE}"
    const val ESPRESSO_CONTRIB = "androidx.test.espresso:espresso-contrib:${TestLibraryVersion.ESPRESSO_CORE}"
    const val ESPRESSO_IDLING_RESOURCES = "androidx.test.espresso:espresso-idling-resource:${TestLibraryVersion.ESPRESSO_CORE}"

    const val MOCKITO_INLINE = "org.mockito:mockito-inline:${TestLibraryVersion.MOCKITO}"
    const val MOCKITO_ANDROID = "org.mockito:mockito-android:${TestLibraryVersion.MOCKITO}"
    const val MOCKITO_KOTLIN = "com.nhaarman.mockitokotlin2:mockito-kotlin:${TestLibraryVersion.MOCKITO_KOTLIN}"
    const val MOCKITO_CORE = "org.mockito:mockito-core:${TestLibraryVersion.MOCKITO_CORE}"
    const val MOCKITO_DEXMAKER = "com.linkedin.dexmaker:dexmaker-mockito:${TestLibraryVersion.MOCKITO_DEXMAKER}"

    const val COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${CoreVersion.COROUTINES_ANDROID}"
    const val ANDROID_X_CORE_TESTING = "android.arch.core:core-testing:${TestLibraryVersion.ANDROID_X_TEST}"
    const val ANDROID_X_TEST_RULES = "androidx.test:rules:${TestLibraryVersion.ANDROID_X_TEST}"
    const val FRAGMENT_TEST = "androidx.fragment:fragment-testing:${TestLibraryVersion.FRAGMENT_TEST}"

    //koin test
    const val KOIN_TEST = "org.koin:koin-test:${TestLibraryVersion.koin_version}"

    //JVM Testing
    const val ANDROID_X_KTX_TESTING = "androidx.test:core-ktx:${TestLibraryVersion.ANDROID_X_TEST}"
    const val ROBOELECTRIC_TEST = "org.robolectric:robolectric:${TestLibraryVersion.ROBOELECTRIC_TEST}"
    const val ANDROID_X_EXT_TESTING ="androidx.test.ext:junit:${TestLibraryVersion.EXT_KOTLIN_RUNNER}"

    const val ANDROID_X_CORE = "androidx.test:core:${TestLibraryVersion.ANDROID_X_TEST}"

    const val DATA_BINDING_COMPILER = "androidx.databinding:databinding-compiler:${TestLibraryVersion.DATA_BINDING_COMPILER}"
}
