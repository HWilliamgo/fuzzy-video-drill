/**
 * @date: 2021/9/10
 * @author: HWilliamgo
 * @description: 描述项目依赖
 */

object Versions {
    const val lifecycle_version     = "2.3.0"
    const val coroutines_android    = "1.3.9"
    const val kotlin_stdlib         = "1.4.0"
    const val core_ktx              = "1.3.2"
    const val app_compat            = "1.2.0"
    const val constraintlayout      = "2.0.4"
    const val material              = "1.2.1"
    const val junit                 = "4.12"
    const val androidx_junit        = "1.1.2"
    const val espresso_core         = "3.3.0"
    const val utilcodex             = "1.25.9"
    const val fastpermission_x      = "1.0.0"
    const val websocket             = "1.4.0"
}

object Dependencies {
    const val viewmodel_ktx         = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle_version}"
    const val livedata_ktx          = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle_version}"
    const val lifecycle_runtime_ktx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle_version}"
    const val coroutines_android    = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines_android}"
    const val kotlin_stdlib         = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin_stdlib}"
    const val core_ktx              = "androidx.core:core-ktx:${Versions.core_ktx}"
    const val app_compat            = "androidx.appcompat:appcompat:${Versions.app_compat}"
    const val constraintlayout      = "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
    const val material              = "com.google.android.material:material:${Versions.material}"
    const val junit                 = "junit:junit:${Versions.junit}"
    const val androidx_junit        = "androidx.test.ext:junit:${Versions.androidx_junit}"
    const val espresso_core         = "androidx.test.espresso:espresso-core:${Versions.espresso_core}"
    const val utilcodex             = "com.blankj:utilcodex:${Versions.utilcodex}"
    const val fastpermission_x      = "com.hwilliamgo:fastpermission-x:${Versions.fastpermission_x}"
    const val websocket             = "org.java-websocket:Java-WebSocket:${Versions.websocket}"
}