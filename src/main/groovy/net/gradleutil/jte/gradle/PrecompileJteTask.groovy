package net.gradleutil.jte.gradle


import gg.jte.TemplateEngine
import gg.jte.html.HtmlPolicy
import gg.jte.resolve.DirectoryCodeResolver
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

import javax.inject.Inject
import java.nio.file.Path
import java.util.concurrent.TimeUnit

abstract class PrecompileJteTask extends JteTaskBase {

    private String htmlPolicyClass
    private String[] compileArgs

    @InputDirectory
    abstract DirectoryProperty getTempDir()

    @Input
    @Optional
    abstract Property<FileCollection> getCompilePath()

    @Input
    @Optional
    abstract Property<String> getHtmlPolicyClass()

    @Input
    @Optional
    String[] getCompileArgs() {
        return compileArgs
    }

    void setCompileArgs(String[] compileArgs) {
        this.compileArgs = compileArgs
    }

    @Inject
    PrecompileJteTask(Project project) {
        tempDir.convention(project.layout.buildDirectory.dir('jteFiles'))
        dependsOn project.tasks.register('copyAllButTags', Copy) {
            from sourceDirectory
            into tempDir.get().asFile.path
            exclude("**/tag")
        }
        dependsOn project.tasks.register('copyTags', Copy) {
            from project.fileTree(sourceDirectory).include("**/tag/*").files
            into tempDir.get().asFile.path + '/tag'
        }
    }

    @TaskAction
    void execute() {
        Logger logger = getLogger()
        long start = System.nanoTime()

        logger.info("Precompiling jte templates found in " + sourceDirectory.asFile.get())

        Path targetPath = targetDirectory.asFile.get().toPath()
        tempDir.getAsFile().get().deleteOnExit()
        Path sourcePath = tempDir.get().asFile.toPath()
        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(sourcePath), targetPath, contentType.get())
        templateEngine.setTrimControlStructures(trimControlStructures.get())
        templateEngine.setHtmlTags(htmlTags)
        templateEngine.setHtmlAttributes(htmlAttributes)
        if (htmlPolicyClass != null) {
            templateEngine.setHtmlPolicy(createHtmlPolicy(htmlPolicyClass))
        }
        templateEngine.setHtmlCommentsPreserved(htmlCommentsPreserved.getOrElse(true))
        templateEngine.setBinaryStaticContent(binaryStaticContent.getOrElse(true))
        templateEngine.setCompileArgs(compileArgs)

        int amount
        try {
            templateEngine.cleanAll()
            List<String> compilePathFiles = compilePath.getOrNull()?.collect { it.absolutePath }
            def files = templateEngine.precompileAll(compilePathFiles)
            amount = files.size()
        } catch (Exception e) {
            logger.error("Failed to precompile templates.", e)
            throw e
        }

        long end = System.nanoTime()
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start)
        logger.info("Successfully precompiled " + amount + " jte file" + (amount == 1 ? "" : "s") + " in " + duration + "s to " + targetDirectory)
    }

    private HtmlPolicy createHtmlPolicy(String htmlPolicyClass) {
        try {
            URLClassLoader projectClassLoader = createProjectClassLoader()
            Class<?> clazz = projectClassLoader.loadClass(htmlPolicyClass)
            return (HtmlPolicy) clazz.getConstructor().newInstance()
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate custom HtmlPolicy " + htmlPolicyClass, e)
        }
    }

    private URLClassLoader createProjectClassLoader() throws IOException {
        List<File> files = new ArrayList<>(compilePath.get().getFiles())

        URL[] runtimeUrls = new URL[files.size()]
        for (int i = 0; i < files.size(); i++) {
            File element = files.get(i)
            runtimeUrls[i] = element.toURI().toURL()
        }
        return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader())
    }
}
