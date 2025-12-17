plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "net.cytonic"
version = "1.0"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("xyz.jpenilla.run-paper:xyz.jpenilla.run-paper.gradle.plugin:3.0.2")
}

gradlePlugin {
    plugins {
        create("runCytosis") {
            id = "net.cytonic.run-cytosis"
            implementationClass = "net.cytonic.runCytosis.RunCytosisPlugin"
            displayName = "Run Cytosis"
            description = "Gradle plugin to run Cytosis servers for development"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "local"
            url = uri(layout.buildDirectory.dir("repo"))
        }
        maven {
            name = "FoxikleCytonicRepository"
            url = uri("https://repo.foxikle.dev/cytonic")
            var u = System.getenv("REPO_USERNAME")
            var p = System.getenv("REPO_PASSWORD")

            if (u == null || u.isEmpty()) {
                u = "no-value-provided"
            }
            if (p == null || p.isEmpty()) {
                p = "no-value-provided"
            }

            val user = providers.gradleProperty("FoxikleCytonicRepositoryUsername").orElse(u).get()
            val pass = providers.gradleProperty("FoxikleCytonicRepositoryPassword").orElse(p).get()
            credentials {
                username = user
                password = pass
            }
            authentication {
                create<BasicAuthentication>("basic") {

                }
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xsuppress-version-warnings")
    }
}