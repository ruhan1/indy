package org.commonjava.aprox.core.expire;

import javax.enterprise.event.Event;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AproxScheduleListener
    implements SchedulerListener
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Event<SchedulerEvent> eventDispatcher;

    private final Scheduler scheduler;

    public AproxScheduleListener( final Scheduler scheduler, final Event<SchedulerEvent> eventDispatcher )
    {
        this.scheduler = scheduler;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void jobScheduled( final Trigger trigger )
    {
        final JobKey key = trigger.getJobKey();
        try
        {
            final JobDetail detail = scheduler.getJobDetail( key );
            eventDispatcher.fire( ScheduleManager.createEvent( SchedulerEventType.SCHEDULE, detail ) );
        }
        catch ( final SchedulerException e )
        {
            logger.error( "Cannot find scheduler job for key: " + key, e );
        }
    }

    @Override
    public void jobUnscheduled( final TriggerKey triggerKey )
    {
        try
        {
            final Trigger trigger = scheduler.getTrigger( triggerKey );
            final JobKey key = trigger.getJobKey();

            final JobDetail detail = scheduler.getJobDetail( key );
            eventDispatcher.fire( ScheduleManager.createEvent( SchedulerEventType.CANCEL, detail ) );
        }
        catch ( final SchedulerException e )
        {
            logger.error( "Cannot find scheduler job for key: " + triggerKey, e );
        }
    }

    @Override
    public void triggerFinalized( final Trigger trigger )
    {
    }

    @Override
    public void triggerPaused( final TriggerKey triggerKey )
    {
    }

    @Override
    public void triggersPaused( final String triggerGroup )
    {
    }

    @Override
    public void triggerResumed( final TriggerKey triggerKey )
    {
    }

    @Override
    public void triggersResumed( final String triggerGroup )
    {
    }

    @Override
    public void jobAdded( final JobDetail jobDetail )
    {
    }

    @Override
    public void jobDeleted( final JobKey jobKey )
    {
    }

    @Override
    public void jobPaused( final JobKey jobKey )
    {
    }

    @Override
    public void jobsPaused( final String jobGroup )
    {
    }

    @Override
    public void jobResumed( final JobKey jobKey )
    {
    }

    @Override
    public void jobsResumed( final String jobGroup )
    {
    }

    @Override
    public void schedulerError( final String msg, final SchedulerException cause )
    {
    }

    @Override
    public void schedulerInStandbyMode()
    {
    }

    @Override
    public void schedulerStarted()
    {
    }

    @Override
    public void schedulerStarting()
    {
    }

    @Override
    public void schedulerShutdown()
    {
    }

    @Override
    public void schedulerShuttingdown()
    {
    }

    @Override
    public void schedulingDataCleared()
    {
    }
}