package org.jenkinsci.plugins.gitclient;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

import com.cloudbees.plugins.credentials.common.StandardCredentials;

import hudson.EnvVars;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.util.ArgumentListBuilder;

/**
 * Implementation class using command line CLI git ran as external command. 
 * This implementation is based on CliGitApiImpl, but uses the clone command for cloning instead of init+fetch.
 * This is faster than init+fetch for bigger repositories with lots of commits and tags.
 * <b>
 * For internal use only, don't use directly. See {@link org.jenkinsci.plugins.gitclient.Git}
 * </b>
 *  * @author <a href="mailto:m.doppler@gmail.com">Michael Doppler</a>
 */
public class CliGitUsingCloneAPIImpl extends CliGitAPIImpl {

    protected static final String DEFAULT_ORIGIN = "origin";

    /**
     * Constructor for CliGitWithCloneAPIImpl.
     *
     * @param gitExe a {@link java.lang.String} object.
     * @param workspace a {@link java.io.File} object.
     * @param listener a {@link hudson.model.TaskListener} object.
     * @param environment a {@link hudson.EnvVars} object.
     */
    protected CliGitUsingCloneAPIImpl(String gitExe, File workspace,
                         TaskListener listener, EnvVars environment) {
        super("git", workspace, listener, environment);
    }

    /**
     * clone_. Implemented using git clone. The base class uses git init + git fetch for cloning instead.
     *
     * @return a {@link org.jenkinsci.plugins.gitclient.CloneCommand} object.
     */
    public CloneCommand clone_() {
        return new CloneCommand() {
            String url;
            String origin = DEFAULT_ORIGIN;
            String reference;
            boolean shallow = false;
            boolean shared = false;
            Integer timeout;
            List<RefSpec> refspecs;
            Integer depth = 1;

            public CloneCommand url(String url) {
                this.url = url;
                return this;
            }

            public CloneCommand repositoryName(String name) {
                this.origin = name;
                return this;
            }

            public CloneCommand shared() {
                return shared(true);
            }

            @Override
            public CloneCommand shared(boolean shared) {
                this.shared = shared;
                return this;
            }

            public CloneCommand shallow() {
                return shallow(true);
            }

            @Override
            public CloneCommand shallow(boolean shallow) {
                this.shallow = shallow;
                return this;
            }

            public CloneCommand noCheckout() {
                return this;
            }

            public CloneCommand tags(boolean tags) {
                // tags are always fetched with clone
                return this;
            }

            public CloneCommand reference(String reference) {
                this.reference = reference;
                return this;
            }

            public CloneCommand timeout(Integer timeout) {
                this.timeout = timeout;
                return this;
            }

            public CloneCommand depth(Integer depth) {
                this.depth = depth;
                return this;
            }

            public CloneCommand refspecs(List<RefSpec> refspecs) {
                this.refspecs = new ArrayList<>(refspecs);
                return this;
            }

            public void execute() throws GitException, InterruptedException {
                listener.getLogger().println("Cloning repository " + url);
                
                URIish urIish = parseURI();
                deleteWorkspaceContents();
                launchCloneCommand(urIish);
                addRemoteRefSpecs();
            }

            private void addRemoteRefSpecs() throws InterruptedException {
                if (refspecs != null) {
                    for (RefSpec refSpec : refspecs) {
                        launchCommand("config", "--add", "remote." + origin + ".fetch", refSpec.toString());
                    }
                }
            }

            private URIish parseURI() {
                URIish urIish = null;
                try {
                    urIish = new URIish(url);
                } catch (URISyntaxException e) {
                    listener.getLogger().println("Invalid repository " + url);
                    throw new IllegalArgumentException("Invalid repository " + url, e);
                }
                return urIish;
            }

            private void deleteWorkspaceContents() {
                try {
                    Util.deleteContentsRecursive(workspace);
                } catch (Exception e) {
                    e.printStackTrace(listener.error("Failed to clean the workspace"));
                    throw new GitException("Failed to delete workspace", e);
                }
            }

            private boolean hasReference() {
                return reference != null && !reference.isEmpty() && new File( reference ).exists();
            }

            private void launchCloneCommand(URIish urIish) throws InterruptedException {
                ArgumentListBuilder args = new ArgumentListBuilder();
                args.add("clone");
                args.add("--no-checkout");
                
                if( hasReference() ) {
                    args.add( "--reference" );
                    args.add( reference );
                    listener.getLogger().println("Using reference repository: " + reference);
                    if( shared ) {
                        listener.getLogger().println("[WARNING] Both shared and reference is used, shared is ignored.");
                    }
                } else if( shared ) {
                    args.add("--shared");
                }
                
                if( shallow ) {
                    args.add("--depth");
                    args.add( depth );
                }
                
                if( !origin.equals( DEFAULT_ORIGIN ) ) {
                    args.add( "--origin" );
                    args.add( origin );
                }

                if (isAtLeastVersion(1,7,1,0)) {
                    args.add("--progress");
                }
                args.add(urIish.toString());
                args.add(workspace.getAbsolutePath());

                StandardCredentials cred = getCredentials().get(urIish.toPrivateString());
                if (cred == null) {
                    cred = getDefaultCredentials();
                }

                launchCommandWithCredentials(args, workspace.getParentFile(), cred, urIish, timeout);
            }
        
        };
    }    

}
