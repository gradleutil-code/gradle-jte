package net.gradleutil.jte.gradle

import gg.jte.ContentType
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory

abstract class JteTaskBase extends DefaultTask {

    protected String[] htmlTags
    protected String[] htmlAttributes

    @InputDirectory
    abstract DirectoryProperty getSourceDirectory();

    @OutputDirectory
    @Optional
    abstract DirectoryProperty getTargetDirectory()

    @Input
    abstract Property<ContentType> getContentType()

    @Input
    @Optional
    abstract Property<Boolean> getTrimControlStructures();

    @Input
    @Optional
    String[] getHtmlTags() {
        return htmlTags
    }

    void setHtmlTags(String[] value) {
        htmlTags = value
    }

    @Input
    @Optional
    String[] getHtmlAttributes() {
        return htmlAttributes
    }

    void setHtmlAttributes(String[] value) {
        htmlAttributes = value
    }

    @Input
    @Optional
    abstract Property<Boolean> getHtmlCommentsPreserved()


    @Input
    @Optional
    abstract Property<Boolean> getBinaryStaticContent()
    
    JteTaskBase(){
        contentType.convention(ContentType.Plain)
        trimControlStructures.convention(false)
    }

}
