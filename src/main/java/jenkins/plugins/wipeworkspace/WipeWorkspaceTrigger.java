package jenkins.plugins.wipeworkspace;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.AbstractProject;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import antlr.ANTLRException;


public class WipeWorkspaceTrigger extends Trigger<AbstractProject<?, ?>>
{
    /* TODO: Set these via global configuration options. */
    private static final int NIGHT_HOUR_START = 22; /* Night starts at 10pm */
    private static final int NIGHT_HOUR_END   = 05; /* Night ends at 10pm */
    
    private static Logger LOGGER = Logger.getLogger(WipeWorkspaceTrigger.class.getName());
    
    private static final Random random = new Random();
    
    private transient AbstractProject<?, ?> project;
    
    @DataBoundConstructor
    public WipeWorkspaceTrigger() throws ANTLRException
    {
        super();
    }
    
    @Override
    public void start(AbstractProject<?, ?> project, boolean newInstance)
    {
        super.start(project, newInstance);
        this.project = project;
        
        scheduleNightly();
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
        LOGGER.log(Level.INFO, "Executing nightly wipe and build for " + job.getName());
        super.run();
        
        job.scheduleBuild(new WipeWorkspaceCause());
    }
    
    private void scheduleNightly()
    {
        int estimatedDurationInHours = Math.min(1, (int) ((double)job.getEstimatedDuration()/(1000 * 60 * 60)));
        int latestHourToRunAt = getLengthOfNightInHours() - estimatedDurationInHours;
        
        int hour = random.nextInt(latestHourToRunAt);
        int minute = random.nextInt(59);
        
        setCronTab(minute + " " + hour + " * * *");
        //setCronTab("*" + " " + "*" + " * * *");
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        
        Formatter formatter = new Formatter(new StringBuilder());
        LOGGER.log(Level.INFO, formatter.format("Job %s scheduled to run at %2$tH:%2$tM every night.", job.getName(), calendar).toString());
    }
    
    private void setCronTab(String cronTabSpec)
    {
        try
        {
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
        if (diff < 0) diff = 24 - (-1*diff);
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
            return "Wipe workspace and trigger clean build nightly"; /* TODO: I18N */
        }
    }
}
