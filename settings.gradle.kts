pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Zainotes"
include(":app")

try {
    val previewScript = file("preview-server.cjs")
    if (previewScript.exists()) {
        val isListening = try {
            java.net.Socket("127.0.0.1", 3000).use { true }
        } catch (_: Exception) {
            false
        }
        if (!isListening) {
            ProcessBuilder(
                "sh",
                "-c",
                "nohup node \"${previewScript.absolutePath}\" >/tmp/preview-server.log 2>&1 < /dev/null &"
            )
                .directory(rootDir)
                .start()
        }
    }
} catch (_: Exception) {
    // Ignore in environments without Node
}

