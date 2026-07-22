pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.experimental.jvm-ecosystem").version("0.1.62")
}

rootProject.name = "mimicvm"

include("annotations")
include("cli")
include("compiler")
include("shared")
include("transformer")
include("vm")

defaults {
    javaLibrary {
        javaVersion = 21

        testing {
            testJavaVersion = 21

            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:6.0.0")
                runtimeOnly("org.junit.platform:junit-platform-launcher:6.0.0")
            }
        }
    }
}
