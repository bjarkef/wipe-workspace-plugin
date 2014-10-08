package jenkins.plugins.wipeworkspace;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.AbstractProject;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import antlr.ANTLRException;


public class WipeWorkspaceTrigger extends Trigger<AbstractProject<?, ?>>
{
    /* TODO: Set these via global configuration options. */
    private static final int NIGHT_HOUR_START = 22; /* Night starts at 10:00pm */
    private static final int NIGHT_HOUR_END = 05; /* Night ends at 5:59am */
    
    private static Logger LOGGER = Logger.getLogger(WipeWorkspaceTrigger.class.getName());
    
    private static final Random random = new Random();
    
    private final String schedule;
    
    @DataBoundConstructor
    public WipeWorkspaceTrigger(String schedule) throws ANTLRException 
    {
        super();
        if (schedule == "") {
            schedule = getNightlySchedule();
        }
        this.schedule = schedule;
    }
    
    @Override
    public void start(AbstractProject<?, ?> project, boolean newInstance)
    {
        super.start(project, newInstance);
        
        setCronTab(this.schedule);
        /* TODO: Persist result of nightly schedule, and use that in the future. */
    }
    
    @Override
    public void stop()
    {
        super.stop();
    }
    
    @Override
    public void run()
    {
        LOGGER.log(Level.INFO, "Executing periodic wipe and build for " + job.getName());
        super.run();
        
        job.scheduleBuild(new WipeWorkspaceCause());
    }
    
    private String getNightlySchedule()
    {
        int estimatedDurationInHours = Math.min(1, (int) ((double) job.getEstimatedDuration() / (1000 * 60 * 60)));
        int latestHourToRunAt = getLengthOfNightInHours() - estimatedDurationInHours;
        
        int hour = random.nextInt(latestHourToRunAt);
        int minute = random.nextInt(59);
        
        return (minute + " " + hour + " * * *");
    }
    
    private void setCronTab(String cronTabSpec)
    {
        try
        {
            LOGGER.log(Level.INFO, "Setting nightly wipe and build for " + job.getName() + " schedule to '"
                                   + cronTabSpec + "'.");
            this.tabs = CronTabList.create(cronTabSpec);
        }
        catch (ANTLRException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private int getLengthOfNightInHours()
    {
        int diff = NIGHT_HOUR_END - NIGHT_HOUR_START;
        if (diff < 0) diff = 24 - (-1 * diff);
        return diff;
    }
    
    
    @Extension
    public static final class WipeWorkspaceTriggerDescriptor extends TriggerDescriptor
    {
        @Override
        public boolean isApplicable(Item item)
        {
            return (item instanceof AbstractProject<?, ?>);
        }
    
        @Override
        public String getDisplayName()
        {
            return "Wipe the workspace and trigger a build periodically"; 
            /* TODO: I18N */
        }
    }
}
