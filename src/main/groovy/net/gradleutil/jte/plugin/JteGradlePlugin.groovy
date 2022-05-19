package net.gradleutil.jte

import groovy.transform.AnnotationCollector
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import static net.gradleutil.conf.util.ConfUtil.setBeanFromConfigFile

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
@AnnotationCollector
@interface ToStringIncludeNames { }

@interface Optional { }

@ToStringIncludeNames
class JteGradlePlugin {

    JteGradlePlugin(){ }

    JteGradlePlugin(File conf, File confOverride){
        setBeanFromConfigFile(this, conf, confOverride)
    }

    List<PluginProperty> pluginProperties 

    Extension extension 

    String implementationClass 

    String description 

    String id 

    List<Task> tasks 

    Task htmlAttributes 

}

@ToStringIncludeNames
class PluginProperty {

    PluginProperty(){ }

    String name 

    String type 

}

@ToStringIncludeNames
class Extension {

    Extension(){ }

    Jte jte 

}

@ToStringIncludeNames
class Jte {

    Jte(){ }

    List<String> htmlTags 

    Boolean binaryStaticContent 

    String sourceDirectory 

    String targetDirectory 

    Boolean trimControlStructures 

    String contentType 

    Boolean htmlCommentsPreserved 

}

@ToStringIncludeNames
class Task {

    Task(){ }

    String name 

    String type 

}
