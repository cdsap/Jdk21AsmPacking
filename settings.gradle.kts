pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity") version "4.3.1"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.4.0"
}

val isCI = providers.environmentVariable("CI").isPresent

develocity {
    server = "https://ge.solutions-team.gradle.com"
    buildScan {
        uploadInBackground = !isCI
        publishing.onlyIf { it.isAuthenticated }
        obfuscation {
            ipAddresses { addresses -> addresses.map { "0.0.0.0" } }
        }
    }
}

rootProject.name="androidTriangle21modules"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
include (":layer_0:module_0_1")
include (":layer_0:module_0_2")
include (":layer_0:module_0_3")
include (":layer_0:module_0_4")
include (":layer_0:module_0_5")
include (":layer_0:module_0_6")
include (":layer_0:module_0_7")
include (":layer_0:module_0_8")
include (":layer_0:module_0_9")
include (":layer_0:module_0_10")
include (":layer_1:module_1_11")
include (":layer_1:module_1_12")
include (":layer_1:module_1_13")
include (":layer_1:module_1_14")
include (":layer_1:module_1_15")
include (":layer_2:module_2_16")
include (":layer_2:module_2_17")
include (":layer_2:module_2_18")
include (":layer_3:module_3_19")
include (":layer_3:module_3_20")
include (":layer_4:module_4_21")
include (":layer_5:module_5_22")