package jenkins.plugins.wipeworkspace;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import hudson.model.AbstractModelObject;
import hudson.model.AbstractProject;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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
        
        public AbstractModelObject getParentObject()
        {
            return target;
        }
        
        public void doWipeAndBuild(StaplerRequest request, StaplerResponse response) throws IOException, ServletException, InterruptedException
        {
            target.doDoWipeOutWorkspace();
            target.doBuild(request, response);
        }
        
        public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException, ServletException, InterruptedException
        {
            doWipeAndBuild(request, response);
        }
        
    }
    
    
    @Override
    @SuppressWarnings("rawtypes") 
    public Collection<? extends Action> createFor(AbstractProject target)
    {
        return Collections.singleton(new WipeWorkspaceProjectAction(target));
    }

}
