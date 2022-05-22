package net.gradleutil.jte.gradle

import gg.jte.TemplateEngine
import gg.jte.resolve.DirectoryCodeResolver
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

abstract class GenerateJteTask extends JteTaskBase {

    GenerateJteTask() {
        targetDirectory.convention(project.layout.buildDirectory.dir("generated-sources/jte"))
    }

    @TaskAction
    void execute() {
        Logger logger = getLogger()
        long start = System.nanoTime()

        logger.info("Generating jte templates found in " + sourceDirectory)

        Path sourcePath = sourceDirectory.toAbsolutePath()
        Path targetPath = targetDirectory.toAbsolutePath()
        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(sourcePath), targetPath, contentType.get())
        templateEngine.setTrimControlStructures(Boolean.TRUE == trimControlStructures)
        templateEngine.setHtmlTags(htmlTags)
        templateEngine.setHtmlAttributes(htmlAttributes)
        templateEngine.setHtmlCommentsPreserved(Boolean.TRUE == htmlCommentsPreserved)
//        templateEngine.setBinaryStaticContent(Boolean.TRUE.equals(binaryStaticContent));

        int amount
        try {
            templateEngine.cleanAll()
            amount = templateEngine.generateAll().size()
        } catch (Exception e) {
            logger.error("Failed to generate templates.", e)

            throw e
        }

        long end = System.nanoTime()
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start)
        logger.info("Successfully generated " + amount + " jte file" + (amount == 1 ? "" : "s") + " in " + duration + "s to " + targetDirectory)

        getProject().getTasks().withType(SourceTask).each { sourceTask ->
            sourceTask.include(targetDirectory.toAbsolutePath().toString())
        }
    }

}
