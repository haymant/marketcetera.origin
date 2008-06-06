package org.marketcetera.photon.actions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.marketcetera.core.ClassVersion;
import org.marketcetera.marketdata.IMarketDataFeed;
import org.marketcetera.marketdata.IMarketDataFeedCredentials;
import org.marketcetera.marketdata.IMarketDataFeedFactory;
import org.marketcetera.photon.PhotonPlugin;
import org.marketcetera.photon.marketdata.IMarketDataConstants;
import org.marketcetera.photon.marketdata.MarketDataFeedService;
import org.marketcetera.photon.marketdata.MarketDataFeedTracker;
import org.marketcetera.quickfix.ConnectionConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/* $License$ */

/**
 * Creates connections to market data feed plug-ins.
 *
 * @author gmiller
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since 0.5.0
 */
@ClassVersion("$Id$")
public class ReconnectMarketDataFeedJob extends Job {

	private static AtomicBoolean reconnectInProgress = new AtomicBoolean(false);
	private BundleContext bundleContext;
	MarketDataFeedTracker marketDataFeedTracker;

	
	public ReconnectMarketDataFeedJob(String name) {
		super(name);
		bundleContext = PhotonPlugin.getDefault().getBundleContext();
		marketDataFeedTracker = new MarketDataFeedTracker(bundleContext);
		marketDataFeedTracker.open();

	}

	@SuppressWarnings("unchecked") // cast on Class.forName()
	@Override
	protected IStatus run(IProgressMonitor monitor) 
	{
		PhotonPlugin plugin = PhotonPlugin.getDefault();
		Logger logger = plugin.getMainLogger();
		if (reconnectInProgress.getAndSet(true)){
			return Status.CANCEL_STATUS;
		}
		boolean succeeded = false;
		try {
			disconnect(marketDataFeedTracker);
		} catch (Throwable th) {
			logger.warn("Could not disconnect from quote feed");
		}
		try {
			IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
	    	IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(IMarketDataConstants.EXTENSION_POINT_ID);
	    	IExtension[] extensions = extensionPoint.getExtensions();

	    	Set<String> startupFeeds = getStartupFeeds();

	    	if (logger.isDebugEnabled()) { 
	    	    logger.debug(String.format("Marketdata: examining %d extensions",
	    	                               extensions.length)); 
	    	}
	    	if (extensions != null && 
	    	    extensions.length > 0) {
	    		for(IExtension anExtension : extensions) {
	    			String pluginName = anExtension.getContributor().getName();
					if (startupFeeds.contains(pluginName)) {
				    	if (logger.isDebugEnabled()) { logger.debug("Marketdata: using "+pluginName); }
		    			IConfigurationElement[] configurationElements = anExtension.getConfigurationElements();
			    		IConfigurationElement feedElement = configurationElements[0];
			    		// construct the market data feed factory corresponding to this extension
			    		String feedFactoryClassName = feedElement.getAttribute(IMarketDataConstants.FEED_FACTORY_CLASS_ATTRIBUTE);                        
			    		Class<IMarketDataFeedFactory> feedClass = (Class<IMarketDataFeedFactory>)Class.forName(feedFactoryClassName, 
			    		                                                                                       true, 
			    		                                                                                       PhotonPlugin.class.getClassLoader());
			    		Constructor<IMarketDataFeedFactory> feedConstructor;
			    		try {
			    		    feedConstructor = feedClass.getConstructor( new Class[0] );
			    		} catch (NoSuchMethodException e) {
			    		    logger.error(String.format("There must exist a default constructor which takes no arguments for feed factory class %s",
			    		                               feedFactoryClassName));
			    		    throw e;
			    		}
                        IMarketDataFeedFactory feedFactory = feedConstructor.newInstance(new Object[0]);
			    		logger.debug(String.format("Feedfactory %s created",
			    		                           feedFactory));
			    		// now construct the credentials object for this feed
                        String credentialsFactoryClassName = feedElement.getAttribute(IMarketDataConstants.CREDENTIALS_FACTORY_CLASS_ATTRIBUTE);
			    		Class<IMarketDataFeedCredentials> credentialsClass = (Class<IMarketDataFeedCredentials>)Class.forName(credentialsFactoryClassName, 
			    		                                                                                                      true, 
			    		                                                                                                      PhotonPlugin.class.getClassLoader());
			    		// retrieve the scoped preferences 
			    		ScopedPreferenceStore preferences = new ScopedPreferenceStore(new InstanceScope(),
			    		                                                              pluginName);
			    		logger.debug(String.format("Retrieved preferences %s for plugin: %s",
			    		                           preferences,
			    		                           pluginName));
			    		// execute a static getter to create the credentials object
			    		Method credentialsGetter;
                        try {
                            credentialsGetter = credentialsClass.getMethod("getInstance", 
                                                                           new Class[] { ScopedPreferenceStore.class } );
                        } catch (NoSuchMethodException e) {
                            logger.error(String.format("There must exist a static method named \"getInstance(ScopedPreferenceStore)\" for credentials class %s",
                                                       credentialsFactoryClassName));
                            throw e;
                        }
                        IMarketDataFeedCredentials credentials = (IMarketDataFeedCredentials)credentialsGetter.invoke(credentialsClass,
                                                                                           preferences);
			    		logger.debug(String.format("Credentials %s created",
			    		                           credentials));
			    		// create the feed object itself
			    		IMarketDataFeed targetQuoteFeed = feedFactory.getMarketDataFeed(credentials);
			    		logger.debug(String.format("Market feed %s created",
			    		                           targetQuoteFeed));
		    			MarketDataFeedService marketDataFeedService = new MarketDataFeedService(targetQuoteFeed);
		    			marketDataFeedService.afterPropertiesSet();
			    		// Quote feed must be started before registration so
						// that resubscription works properly. See bug #213.
			    		targetQuoteFeed.start();
			    		logger.debug(String.format("Market feed %s started",
			    		                           targetQuoteFeed));
						ServiceRegistration registration = bundleContext.registerService(MarketDataFeedService.class.getName(), 
						                                                                 marketDataFeedService, 
						                                                                 null);
		    			marketDataFeedService.setServiceRegistration(registration);
		    			// announce this market feed to the script registry
		    	        PhotonPlugin.getDefault().getScriptRegistry().connectToMarketDataFeed(targetQuoteFeed);
		    			succeeded = true;
		    			break;
	    			} else {
				    	 logger.warn("Marketdata: not using "+pluginName+" because it is not listed in 'org.marketcetera.photon/marketdata.startup'");
	    			}
				}
			}
	
	    	if (logger.isDebugEnabled()) { logger.debug("Marketdata: done examining "+extensions.length+" extensions"); }
		} catch (Throwable t) {
			logger.error(new StringBuilder("Exception connecting to market data feed: ").append(t));
			return Status.CANCEL_STATUS;
		} finally {
			reconnectInProgress.set(false);
			if (!succeeded){
				logger.error("Error connecting to market data feed");
				return Status.CANCEL_STATUS;
			}
		}
		return Status.OK_STATUS;

	}

	private Set<String> getStartupFeeds() {
		String startupString = PhotonPlugin.getDefault().getPreferenceStore().getString(ConnectionConstants.MARKETDATA_STARTUP_KEY);
		startupString.split("[\\s,]+");
		return new HashSet<String>(Arrays.asList(startupString.split("[\\s,]+")));
	}

	public static void disconnect(MarketDataFeedTracker marketDataFeedTracker) {

		MarketDataFeedService<?> service = marketDataFeedTracker.getMarketDataFeedService();
		if (service != null){
			ServiceRegistration serviceRegistration = service.getServiceRegistration();
			if (serviceRegistration != null){
				serviceRegistration.unregister();
			}
		}

		RuntimeException caughtException = null;
		if (service != null){
			try {
				service.stop();
			} catch (RuntimeException e) {
				caughtException = e;
			}
		}
		if (caughtException != null){
			throw caughtException;
		}
	}
	
	// TODO: refactor this and run()
	@SuppressWarnings("unchecked")
	public static String [][] getFeedNames() {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
    	IExtensionPoint extensionPoint =
    	extensionRegistry.getExtensionPoint(IMarketDataConstants.EXTENSION_POINT_ID);
    	IExtension[] extensions = extensionPoint.getExtensions();

    	String[][] result = new String[extensions.length][2];
    	int i = 0;
    	for (IExtension anExtension : extensions) {
			IContributor contributor = anExtension.getContributor();

			String pluginName = contributor.getName();
			String providerName = pluginName;
			try {
    			IConfigurationElement[] configurationElements = anExtension.getConfigurationElements();
	    		IConfigurationElement feedElement = configurationElements[0];

	    		String factoryClass = feedElement.getAttribute(IMarketDataConstants.FEED_FACTORY_CLASS_ATTRIBUTE);
	    		Class<IMarketDataFeedFactory> clazz = (Class<IMarketDataFeedFactory>) Class.forName(factoryClass, true, PhotonPlugin.class.getClassLoader());
	    		Constructor<IMarketDataFeedFactory> constructor = clazz.getConstructor( new Class[0] );
	    		IMarketDataFeedFactory factory = constructor.newInstance(new Object[0]);
	    		providerName = factory.getProviderName();
			} catch (Exception ex){
				// do nothing.
			}
			
			result[i][0] = providerName;
			result[i][1] = pluginName;
			i++;
		}
    	return result;
	}

}
