@import net.gradleutil.jte.plugin.JteGradlePlugin
@param JteGradlePlugin plugin
plugins{ id('groovy-gradle-plugin') }
${plugin.getId()}
${plugin.getDescription()}