package net.gradleutil.jte.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class JteGradle implements Plugin<Project> {

    private TaskProvider<PrecompileJteTask> precompileJte

    @Override
    void apply(Project project) {
        precompileJte = project.getTasks().register("precompileJte", PrecompileJteTask.class)
        project.getTasks().register("generateJte", GenerateJteTask.class)
    }
}
