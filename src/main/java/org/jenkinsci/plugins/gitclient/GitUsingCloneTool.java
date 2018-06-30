package org.jenkinsci.plugins.gitclient;

import java.util.Collections;
import java.util.List;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.plugins.git.GitTool;
import hudson.tools.ToolProperty;

/**
 * GitUsingCloneTool as {@link hudson.plugins.git.GitTool}
 * @author <a href="mailto:m.doppler@gmail.com">Michael Doppler</a>
 */
public class GitUsingCloneTool extends GitTool {
    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public GitUsingCloneTool(final List<? extends ToolProperty<?>> properties) {
        super(MAGIC_EXENAME, MAGIC_EXENAME, properties);
    }

    public GitUsingCloneTool() {
        this(Collections.<ToolProperty<?>>emptyList());
    }

    /** {@inheritDoc} */
    @Override
    public GitTool.DescriptorImpl getDescriptor() {
        return super.getDescriptor();
    }

    @Extension @Symbol(MAGIC_EXENAME)
    public static class DescriptorImpl extends GitTool.DescriptorImpl {
        @Override
        public String getDisplayName() {
            return "Git using clone";
        }
    }
    
    /**
     * {@link Git} recognizes this as the tool / executable name to use {@link CliGitUsingCloneAPIImpl}.
     */
    public static final String MAGIC_EXENAME = "git-using-clone";
}
