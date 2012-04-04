package jenkins.plugins.wipeworkspace;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.listeners.RunListener;

import java.io.IOException;

@Extension
public class WipeWorkspaceRunListener extends RunListener<AbstractBuild<?, ?>>
{
    @Override
    public Environment setUpEnvironment(AbstractBuild build, Launcher launcher, BuildListener listener)
        throws IOException, InterruptedException
    {
        Environment environment = super.setUpEnvironment(build, launcher, listener);
        if (!causedByNightlyTrigger(build)) return environment;
        
        listener.getLogger().print("Wipeing workspace before building.");
        wipeWorkspace(build);
        
        return environment;
    }
    
    
    boolean causedByNightlyTrigger(AbstractBuild<?, ?> build)
    {
        for(Cause cause : build.getCauses())
            if (cause instanceof WipeWorkspaceCause)
                return true;
        return false;
    }
    
    
    void wipeWorkspace(AbstractBuild<?, ?> build) throws IOException, InterruptedException
    {
        FilePath ws = build.getWorkspace();
        
        if (ws != null)
        {
            build.getProject().getScm().processWorkspaceBeforeDeletion(build.getProject(), ws, build.getBuiltOn());
            ws.deleteRecursive();
        }
    }
}
