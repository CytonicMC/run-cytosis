package net.cytonic.runCytosis

import net.cytonic.runCytosis.tasks.RunCytosis
import org.gradle.api.Plugin
import org.gradle.api.Project

class RunCytosisPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("runCytosis", RunCytosis::class.java) {
            group = "run cytosis"
        }
    }
}