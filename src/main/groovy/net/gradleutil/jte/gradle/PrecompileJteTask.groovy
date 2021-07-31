package net.gradleutil.jte.gradle

import gg.jte.TemplateEngine
import gg.jte.html.HtmlPolicy
import gg.jte.resolve.DirectoryCodeResolver
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.TimeUnit

class PrecompileJteTask extends JteTaskBase {

    private FileCollection compilePath
    private String htmlPolicyClass
    private String[] compileArgs

    @Nested
    FileCollection getCompilePath() {
        return compilePath
    }

    void setCompilePath(FileCollection compilePath) {
        this.compilePath = compilePath
    }

    @Input
    @Optional
    String getHtmlPolicyClass() {
        return htmlPolicyClass
    }

    void setHtmlPolicyClass(String htmlPolicyClass) {
        this.htmlPolicyClass = htmlPolicyClass
    }

    @Input
    @Optional
    String[] getCompileArgs() {
        return compileArgs
    }

    void setCompileArgs(String[] compileArgs) {
        this.compileArgs = compileArgs
    }

    @TaskAction
    void execute() {
        Logger logger = getLogger()
        long start = System.nanoTime()

        logger.info("Precompiling jte templates found in " + sourceDirectory)
        File tempDir = File.createTempDir().tap { deleteOnExit() }
        new AntBuilder().copy(todir: tempDir, verbose:false, quiet:true) {
            fileset(dir: sourceDirectory.toFile()) { exclude(name: "**/tag") }
        }
        new AntBuilder().copy(todir: new File(tempDir, "tag"), flatten: true, verbose:false, quiet:true) {
            fileset(dir: sourceDirectory.toFile()) { include(name: "**/tag/**") }
        }
        sourceDirectory = tempDir.toPath()

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(sourceDirectory), targetDirectory, contentType)
        templateEngine.setTrimControlStructures(Boolean.TRUE.equals(trimControlStructures))
        templateEngine.setHtmlTags(htmlTags)
        templateEngine.setHtmlAttributes(htmlAttributes)
        if (htmlPolicyClass != null) {
            templateEngine.setHtmlPolicy(createHtmlPolicy(htmlPolicyClass))
        }
        templateEngine.setHtmlCommentsPreserved(Boolean.TRUE.equals(htmlCommentsPreserved))
        templateEngine.setBinaryStaticContent(Boolean.TRUE.equals(binaryStaticContent))
        templateEngine.setCompileArgs(compileArgs)

        int amount
        try {
            templateEngine.cleanAll()
            List<String> compilePathFiles = compilePath.collect { it.absolutePath }
            amount = templateEngine.precompileAll(compilePathFiles).size()
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
        List<File> files = new ArrayList<>(compilePath.getFiles())

        URL[] runtimeUrls = new URL[files.size()]
        for (int i = 0; i < files.size(); i++) {
            File element = files.get(i)
            runtimeUrls[i] = element.toURI().toURL()
        }
        return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader())
    }
}
