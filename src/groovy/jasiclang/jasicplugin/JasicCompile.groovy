package jasicplugin

import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.tasks.compile.CompilationFailedException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

import static org.apache.commons.io.FilenameUtils.removeExtension

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
class JasicCompile extends AbstractCompile{
	private static final String JASIC_COMPILER_CLASS_NAME = 'jasiccompiler.JASICCompiler'
	public static final String JASIC_CLASSPATH_FIELD = 'jasicClasspath'
	protected static final String COMPILATION_EXCEPTION_CLASS_NAME = 'jasiccompiler.exceptions.JASICCompilationException'
	FileCollection jasicClasspath

	@TaskAction
	protected void compile(IncrementalTaskInputs inputs){
		compile()
	}

	protected void compile() {
		ensureJasicConfigurationSpecified()
		def compiler = instantiateCompiler()
		source.files.each { file ->
			file.withInputStream { stream ->
				try {
					compiler.compileTo(removeExtension(file.name), stream, destinationDir)
				} catch (Throwable e) {
					if (e.class.name == COMPILATION_EXCEPTION_CLASS_NAME) {
						System.err.println()
						def messages = [e.message, e.cause?.message] + e.problems*.description
						messages.findAll { it }.each { System.err.println(it) }
						throw new CompilationFailedException()
					}
					throw e
				}
			}
		}
	}

	protected void ensureJasicConfigurationSpecified() {
		if (getJasicClasspath().empty) {
			throw new InvalidUserDataException('You must assign a Jasic library to the "jasic" configuration.')
		}
	}

	protected instantiateCompiler() {
		def jasicCompilerClass = loadJasicCompilerClass()
		jasicCompilerClass.getConstructor().newInstance()
	}

	protected Class loadJasicCompilerClass() {
		def jasicClasspathUrls = getJasicClasspath().files.collect { it.toURI().toURL() } as URL[]
		def goloClassLoader = URLClassLoader.newInstance(jasicClasspathUrls, getClass().classLoader)
		goloClassLoader.loadClass(JASIC_COMPILER_CLASS_NAME, true)
	}
}
