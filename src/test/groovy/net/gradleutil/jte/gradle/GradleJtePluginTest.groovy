package net.gradleutil.jte.gradle

import net.gradleutil.jte.gradle.PrecompileJteTask
import org.gradle.api.logging.LogLevel
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GradleJtePluginTest extends Specification {

    def "plugin registers task"() {
        given:
        def project = ProjectBuilder.builder().build()
        def buildDir = project.buildDir
        buildDir.mkdirs()
        new File(buildDir, "schema.json") << '''
        {
          "$schema": "http://json-schema.org/draft-06/schema#",
          "$ref": "#/definitions/Dataobject",
          "definitions": {
            "Dataobject": {
              "type": "object",
              "additionalProperties": false,
              "properties": {
                "$schema": {
                  "type": "string",
                  "title": "Schema",
                  "description": "Pointer to the schema against which this document should be validated."
                },
                "version": {
                  "type": "string"
                }
              }
            }
          }
        }'''.stripIndent()

        when:
        project.plugins.apply("net.gradleutil.gradle-jte")

        then:
        project.tasks.findByName("precompileJte") != null
        def genConfigTask = project.tasks.findByName("precompileJte") as PrecompileJteTask
        genConfigTask != null
    }
    

}
