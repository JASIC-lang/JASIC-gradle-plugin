package jasicplugin

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver

import static org.gradle.util.ConfigureUtil.configure

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
class JasicSourceSet {
	private final SourceDirectorySet jasic
	
	JasicSourceSet(String displayName, FileResolver fileResolver) {
		jasic = new DefaultSourceDirectorySet(String.format('%s Jasic source', displayName), fileResolver)
		jasic.filter.include('**/*.jasic')
	}

	SourceDirectorySet getJasic() {
		jasic
	}

	JasicSourceSet jasic(Closure closure) {
		configure(closure, jasic)
		this
	}
}
