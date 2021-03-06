package hudson.plugins.git;

import hudson.EnvVars;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.DumbSlave;
import hudson.tools.ToolDescriptor;
import hudson.util.StreamTaskListener;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang.SystemUtils;
import org.jenkinsci.plugins.gitclient.GitUsingCloneTool;
import org.jenkinsci.plugins.gitclient.JGitApacheTool;
import org.jenkinsci.plugins.gitclient.JGitTool;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class GitToolTest {

    @ClassRule public static JenkinsRule j = new JenkinsRule();

    private GitTool gitTool;

    @Before
    public void setUp() throws IOException {
        GitTool.onLoaded();
        gitTool = GitTool.getDefaultInstallation();
    }

    @Test
    public void testGetGitExe() {
        assertEquals(SystemUtils.IS_OS_WINDOWS ? "git.exe" : "git", gitTool.getGitExe());
    }

    @Test
    public void testForNode() throws Exception {
        DumbSlave slave = j.createSlave();
        slave.setMode(Node.Mode.EXCLUSIVE);
        TaskListener log = StreamTaskListener.fromStdout();
        GitTool newTool = gitTool.forNode(slave, log);
        assertEquals(gitTool.getGitExe(), newTool.getGitExe());
    }

    @Test
    public void testForEnvironment() {
        EnvVars environment = new EnvVars();
        GitTool newTool = gitTool.forEnvironment(environment);
        assertEquals(gitTool.getGitExe(), newTool.getGitExe());
    }

    @Test
    public void testGetDescriptor() {
        GitTool.DescriptorImpl descriptor = gitTool.getDescriptor();
        assertEquals("Git", descriptor.getDisplayName());
    }

    @Test
    public void testGetInstallationFromDescriptor() {
        GitTool.DescriptorImpl descriptor = gitTool.getDescriptor();
        assertEquals(null, descriptor.getInstallation(""));
        assertEquals(null, descriptor.getInstallation("not-a-valid-git-install"));
    }

    @Test
    public void testGetApplicableFromDescriptor() {
        GitTool.DescriptorImpl gitDescriptor = gitTool.getDescriptor();
        GitTool.DescriptorImpl gitUsingCloneDescriptor = (new GitUsingCloneTool()).getDescriptor();
        GitTool.DescriptorImpl jgitDescriptor = (new JGitTool()).getDescriptor();
        GitTool.DescriptorImpl jgitApacheDescriptor = (new JGitApacheTool()).getDescriptor();
        List<ToolDescriptor<? extends GitTool>> toolDescriptors = gitDescriptor.getApplicableDescriptors();
        assertTrue("git tool descriptor not found in " + toolDescriptors, toolDescriptors.contains(gitDescriptor));
        assertTrue("git using clone tool descriptor not found in " + toolDescriptors, toolDescriptors.contains(gitUsingCloneDescriptor));
        assertTrue("jgit tool descriptor not found in " + toolDescriptors, toolDescriptors.contains(jgitDescriptor));
        assertTrue("jgitapache tool descriptor not found in " + toolDescriptors, toolDescriptors.contains(jgitApacheDescriptor));
        assertEquals("Wrong tool descriptor count in " + toolDescriptors, 4, toolDescriptors.size());
    }

}
