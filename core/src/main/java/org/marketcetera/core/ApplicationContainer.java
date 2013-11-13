package org.marketcetera.core;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.PropertyConfigurator;
import org.marketcetera.util.log.SLF4JLoggerProxy;
import org.marketcetera.util.misc.ClassVersion;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/* $License$ */

/**
 * Provides a process-based application in which to run Marketcetera components.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
@ClassVersion("$Id$")
public class ApplicationContainer
        extends ApplicationBase
        implements ApplicationInfoProvider, Lifecycle
{
    /* (non-Javadoc)
     * @see org.marketcetera.core.ApplicationInfoProvider#getAppDir()
     */
    @Override
    public File getAppDir()
    {
        return new File(APP_DIR);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.core.ApplicationInfoProvider#getConfDir()
     */
    @Override
    public File getConfDir()
    {
        return new File(CONF_DIR);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.core.ApplicationInfoProvider#getContext()
     */
    @Override
    public ConfigurableApplicationContext getContext()
    {
        return context;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.core.ApplicationInfoProvider#getArguments()
     */
    @Override
    public String[] getArguments()
    {
        return arguments;
    }
    /**
     * Sets the application arguments value.
     *
     * @param inArgs a <code>String[]</code> value
     */
    public void setArguments(String...inArgs)
    {
        arguments = inArgs;
    }
    /**
     * Starts application.
     *
     * @param args a <code>String[]</code> value
     */
    public static void main(String[] args)
    {
        // configure logger
        PropertyConfigurator.configureAndWatch(ApplicationBase.CONF_DIR+"log4j.properties",
                                               LOGGER_WATCH_DELAY);
        // log application start
        Messages.APP_COPYRIGHT.info(ApplicationContainer.class);
        Messages.APP_VERSION_BUILD.info(ApplicationContainer.class,
                                        ApplicationVersion.getVersion(),
                                        ApplicationVersion.getBuildNumber());
        Messages.APP_START.info(ApplicationContainer.class);
        final ApplicationContainer application;
        try {
            application = new ApplicationContainer();
            application.setArguments(args);
            application.start();
        } catch(Exception e) {
            e.printStackTrace();
            try {
                Messages.APP_STOP_ERROR.error(ApplicationContainer.class,
                                              e);
            } catch(Exception e2) {
                System.err.println("Reporting failed"); //$NON-NLS-1$
                System.err.println("Reporting failure"); //$NON-NLS-1$
                e2.printStackTrace();
                System.err.println("Original failure"); //$NON-NLS-1$
                e.printStackTrace();
            }
            return;
        }
        Messages.APP_STARTED.info(ApplicationContainer.class);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                application.stop();
                Messages.APP_STOP.info(ApplicationContainer.class);
            }
        });
        try {
            application.startWaitingForever();
        } catch(Exception e) {
            try {
                Messages.APP_STOP_ERROR.error(ApplicationContainer.class,
                                              e);
            } catch(Exception e2) {
                System.err.println("Reporting failed"); //$NON-NLS-1$
                System.err.println("Reporting failure"); //$NON-NLS-1$
                e2.printStackTrace();
                System.err.println("Original failure"); //$NON-NLS-1$
                e.printStackTrace();
            }
            return;
        }
        Messages.APP_STOP_SUCCESS.info(ApplicationContainer.class);
    }
    /**
     * Adds the given shutdown task to the shutdown task collection.
     * 
     * <p>Tasks are executed upon application shutdown in their native order.
     * 
     * @param inTask a <code>ShutdownTask</code> value
     */
    public synchronized static void addShutdownTask(ComparableTask inTask)
    {
        shutdownTasks.add(inTask);
    }
    /**
     * Removes the given shutdown task from the shutdown task collection.
     * 
     * @param inTask a <code>ShutdownTask</code> value
     */
    public synchronized static void removeShutdownTask(ComparableTask inTask)
    {
        shutdownTasks.remove(inTask);
    }
    /**
     * Get the instance value.
     *
     * @return an <code>ApplicationContainer</code> value
     */
    public static ApplicationContainer getInstance()
    {
        return instance;
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#isRunning()
     */
    @Override
    public boolean isRunning()
    {
        return running.get();
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#start()
     */
    @Override
    public synchronized void start()
    {
        instance = this;
        context = generateContext();
        context.registerShutdownHook();
        running.set(true);
    }
    /* (non-Javadoc)
     * @see org.springframework.context.Lifecycle#stop()
     */
    @Override
    public synchronized void stop()
    {
        try {
            for(ComparableTask task : shutdownTasks) {
                try {
                    task.run();
                } catch (Exception e) {
                    SLF4JLoggerProxy.warn(this,
                                          e,
                                          "Error executing shutdown task: {}",
                                          task);
                }
            }
            context.stop();
            context = null;
        } finally {
            running.set(false);
        }
    }
    /**
     * Generates the base application context with which to run.
     *
     * @return a <code>ConfigurableApplicationContext</code> value
     */
    protected ConfigurableApplicationContext generateContext()
    {
        return new FileSystemXmlApplicationContext(new String[] { "file:"+CONF_DIR+"application.xml" }, //$NON-NLS-1$ //$NON-NLS-2$
                                                   null);
    }
    /**
     * indicates if the app is running or not
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /**
     * arguments passed to the cmd line
     */
    private String[] arguments;
    /**
     * Spring application context
     */
    private ConfigurableApplicationContext context;
    /**
     * singleton instance of the application container
     */
    private static ApplicationContainer instance;
    /**
     * collection of tasks to run upon application shutdown
     */
    private static final Set<ComparableTask> shutdownTasks = new TreeSet<ComparableTask>();
}
