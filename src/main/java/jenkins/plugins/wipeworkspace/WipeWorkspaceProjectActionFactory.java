package jenkins.plugins.wipeworkspace;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import hudson.model.AbstractProject;

import java.util.Collection;
import java.util.Collections;

@Extension
public class WipeWorkspaceProjectActionFactory extends TransientProjectActionFactory
{

    public static final class WipeWorkspaceProjectAction implements Action
    {
        AbstractProject<?, ?> target;

        public WipeWorkspaceProjectAction(AbstractProject<?, ?> target)
        {
            this.target = target;
        }

        public String getIconFileName()
        {
            return "clock.png";
        }

        public String getDisplayName()
        {
            return "Wipe and Build"; /* TODO: i18n */
        }

        public String getUrlName()
        {
            return "wipeworkspace";
        }
    }
    
    
    @Override
    @SuppressWarnings("rawtypes") 
    public Collection<? extends Action> createFor(AbstractProject target)
    {
        return Collections.singleton(new WipeWorkspaceProjectAction(target));
    }

}
