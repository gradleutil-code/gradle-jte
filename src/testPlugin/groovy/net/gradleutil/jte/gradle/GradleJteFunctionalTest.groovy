package net.gradleutil.jte.gradle


import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class GradleJteFunctionalTest extends Specification {

    def "generate config bean"() {
        given:
        def projectDir = new File("build/functionalTest")
        projectDir.deleteDir()
        projectDir.mkdirs()
        def jteDir = new File(projectDir, 'jte').tap { mkdirs() }
        def tagDir = new File(jteDir, 'tag').tap { mkdirs() }
        def tagSubDir = new File(tagDir, 'submariner/tag').tap { mkdirs() }
        new File(tagSubDir, "someTagFragment.jte").text = 'another fragment'
        new File(tagDir, "someFragment.jte").text = 'another fragment'
        new File(jteDir, "template.jte").text = 'blah blah'
        new File(projectDir, "settings.gradle").text = ''
        new File(projectDir, "build.gradle") << """
            plugins {
                id('net.gradleutil.gradle-jte')
            }
            def jteDir = layout.buildDirectory.dir("jte-classes")
            def jteSrc = layout.projectDirectory.dir("./jte")
            tasks.precompileJte {
                sourceDirectory = jteSrc.dir('.')
                targetDirectory = jteDir.get()
                // compilePath = sourceSets.main.runtimeClasspath
                // contentType = 'Plain'
            }
        """.stripIndent()

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("precompileJte", "-Si")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains('jte')
    }


}
