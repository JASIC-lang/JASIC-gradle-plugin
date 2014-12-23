package jasicplugin

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention

import javax.inject.Inject

import static jasicplugin.JasicCompile.JASIC_CLASSPATH_FIELD
import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CONFIGURATION_NAME

/**
 * I figured out how gradle plugins work from look at <a href="http://goo.gl/jbn8am">this</a> source code. Might of copied
 * them a lil bit. It's licensed under the Apache License 2.0, which can be viewed <a href="http://goo.gl/tPPd">here</a>. 
 * List of changes:
 * <ul>
 * 	<li>Made for JASIC rather then golo.</li>
 * </ul>
 * Sorry for being lazy on the list of changes. Please don't sue me. Golo is a cool programming language btw.
 * 
 * @author xbony2, Golo-lang
 *
 */
class JasicPlugin implements Plugin<Project>{
	public static final String JASIC_PLUGIN_NAME = "jasic"
	public static final String JASIC_CONFIGURATION_NAME = JASIC_PLUGIN_NAME
	
	Project project
	FileResolver fileResolver
	Configuration jasicConfiguration
	JasicPluginExtension pluginExtension

	@Inject
	JasicPlugin(FileResolver fileResolver) {
		this.fileResolver = fileResolver
	}
	
	@Override
	public void apply(Project project) {
		project = this.project
		project.plugins.apply(JavaPlugin)
		
		configureSourceSetDefaults(project.plugins.getPlugin(JavaBasePlugin))
		configureJasicConfigurationAndClasspath()
		
		addJasicPluginExtension()
	}
	
	private void configureSourceSetDefaults(JavaBasePlugin javaBasePlugin) {
		project.convention.getPlugin(JavaPluginConvention).sourceSets.all { sourceSet ->
			def jasicSourceSet = new JasicSourceSet(sourceSet.displayName, fileResolver)
			new DslObject(sourceSet).convention.plugins.put(JASIC_PLUGIN_NAME, jasicSourceSet)

			jasicSourceSet.jasic.srcDir("src/${sourceSet.name}/jasic")

			def compileTaskName = sourceSet.getCompileTaskName(JASIC_PLUGIN_NAME)

			def jasicCompile = project.tasks.create(compileTaskName, JasicCompile)
			javaBasePlugin.configureForSourceSet(sourceSet, jasicCompile)
			jasicCompile.dependsOn(sourceSet.compileJavaTaskName)
			jasicCompile.setDescription("Compiles the ${sourceSet.name} Groovy source.")
			jasicCompile.setSource(jasicSourceSet.jasic)

			project.tasks.getByName(sourceSet.classesTaskName).dependsOn(compileTaskName)
		}
	}

	private void ensureMainModuleConfigured() {
		if (!pluginExtension.mainModule) {
			throw new InvalidUserDataException('You must specify the mainModule using jasic extension.')
		}
	}
	
	private void configureJasicConfigurationAndClasspath() {
		jasicConfiguration = project.configurations.create(JASIC_CONFIGURATION_NAME)
			.setVisible(false)
			.setDescription('The Jasic libraries to be used for this Jasic project.')

		project.configurations.getByName(RUNTIME_CONFIGURATION_NAME).extendsFrom(jasicConfiguration)

		project.tasks.withType(JasicCompile) { JasicCompile jasicCompile ->
			goloCompile.conventionMapping.map(JASIC_CLASSPATH_FIELD) { jasicConfiguration }
		}
	}

	private void addJasicPluginExtension() {
		pluginExtension = project.extensions.create(JASIC_PLUGIN_NAME, JASICPluginExtension)
	}
}
