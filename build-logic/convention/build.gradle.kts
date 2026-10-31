import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "it.edon.lifeos.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    // Provided at runtime by the root project, which declares these plugins with `apply false`.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.kotlin.composeGradlePlugin)
    implementation(libs.ktlint.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "lifeos.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "lifeos.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "lifeos.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "lifeos.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("quality") {
            id = "lifeos.quality"
            implementationClass = "QualityConventionPlugin"
        }
    }
}
