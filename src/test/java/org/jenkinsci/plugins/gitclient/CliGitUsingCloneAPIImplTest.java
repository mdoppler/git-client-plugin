package org.jenkinsci.plugins.gitclient;

import java.io.File;

public class CliGitUsingCloneAPIImplTest extends CliGitAPIImplTest {

	@Override
	protected GitClient setupGitAPI(File ws) throws Exception {
		return Git.with(listener, env).in(ws).using("git-using-clone").getClient();
	}

	protected CliGitAPIImpl createGitApiForCurrentDirectory() {
		return new CliGitUsingCloneAPIImpl("git", new File("."), listener, env);
	}

}
