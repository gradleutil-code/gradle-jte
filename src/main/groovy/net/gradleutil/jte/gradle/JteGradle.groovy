package net.gradleutil.jte.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class JteGradle implements Plugin<Project> {

    private TaskProvider<PrecompileJteTask> precompileJte;

    @Override
    public void apply(Project project) {
        project.getTasks().register("precompileJte", PrecompileJteTask.class);
        project.getTasks().register("generateJte", GenerateJteTask.class);
    }
}
