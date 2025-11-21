plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
    id("convention.detekt")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
