package net.cytonic.runCytosis.tasks

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import xyz.jpenilla.runtask.task.AbstractRun
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

abstract class RunCytosis : AbstractRun() {

    @get:Input
    abstract val cytosisVersion: Property<String>

    @get:Input
    abstract val cytosisGroup: Property<String>

    @get:Input
    abstract val cytosisArtifact: Property<String>

    @get:Input
    @get:Optional
    abstract val cytosisClassifier: Property<String>

    @get:Input
    @get:Optional
    abstract val cytosisMainClass: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    @get:Optional
    abstract val pluginJar: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val pluginJars: ConfigurableFileCollection

    init {
        displayName.convention("Cytosis")
        cytosisGroup.convention("net.cytonic")
        cytosisArtifact.convention("Cytosis")
        cytosisClassifier.convention("all")
        cytosisMainClass.convention("net.cytonic.cytosis.Cytosis")

        javaLauncher.set(javaToolchainService.launcherFor {
            languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(25))
        })

        version.set(cytosisVersion)
        mainClass.set(cytosisMainClass)
    }

    override fun init() {
        super.init()

        runClasspath.from(project.provider {
            val group = cytosisGroup.get()
            val artifact = cytosisArtifact.get()
            val version = cytosisVersion.get()
            val classifier = cytosisClassifier.orNull

            val cytosisConfig = project.configurations.detachedConfiguration()
            cytosisConfig.isTransitive = false

            val dependency = if (classifier != null) {
                project.dependencies.create("$group:$artifact:$version:$classifier")
            } else {
                project.dependencies.create("$group:$artifact:$version")
            }

            cytosisConfig.dependencies.add(dependency)
            cytosisConfig
        })

        if (!pluginJar.isPresent) {
            val jarTask = project.tasks.findByName("shadowJar")
                ?: project.tasks.findByName("jar")

            if (jarTask is org.gradle.jvm.tasks.Jar) {
                pluginJar.set(jarTask.archiveFile)
            }
        }
    }

    override fun preExec(workingDir: Path) {
        super.preExec(workingDir)

        val pluginsDir = workingDir.resolve("plugins")
        if (!pluginsDir.exists()) {
            pluginsDir.createDirectories()
        }

        if (pluginJar.isPresent) {
            val jar = pluginJar.get().asFile
            if (jar.exists()) {
                val dest = pluginsDir.resolve(jar.name)
                jar.toPath().copyTo(dest, overwrite = true)
                logger.lifecycle("Copied plugin: {}", jar.name)
            } else {
                logger.warn("Plugin jar not found: {}", jar.absolutePath)
            }
        }

        pluginJars.files.forEach { file ->
            if (file.exists()) {
                val dest = pluginsDir.resolve(file.name)
                file.toPath().copyTo(dest, overwrite = true)
                logger.lifecycle("Copied additional plugin: {}", file.name)
            }
        }

        logger.lifecycle("Cytosis version: {}", cytosisVersion.get())
        logger.lifecycle("Run directory: {}", workingDir)
        logger.lifecycle("Plugins directory: {}", pluginsDir)
    }

    fun cytosisVersion(version: String) {
        this.cytosisVersion.set(version)
    }

    fun cytosisMainClass(mainClass: String) {
        this.cytosisMainClass.set(mainClass)
    }

    fun cytosisCoordinates(group: String, artifact: String, classifier: String? = null) {
        this.cytosisGroup.set(group)
        this.cytosisArtifact.set(artifact)
        if (classifier != null) {
            this.cytosisClassifier.set(classifier)
        }
    }

    fun plugin(file: Any) {
        pluginJars.from(file)
    }
}