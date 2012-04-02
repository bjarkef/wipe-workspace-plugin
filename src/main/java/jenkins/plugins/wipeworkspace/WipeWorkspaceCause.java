package jenkins.plugins.wipeworkspace;

import hudson.model.Cause;

public class WipeWorkspaceCause extends Cause
{
    @Override
    public String getShortDescription()
    {
        return "Wipe and Build";
    }
}
