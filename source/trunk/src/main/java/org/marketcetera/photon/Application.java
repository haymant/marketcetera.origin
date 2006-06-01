package org.marketcetera.photon;

import java.math.BigDecimal;

import javax.jms.JMSException;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.marketcetera.core.IDFactory;
import org.marketcetera.core.InMemoryIDFactory;
import org.marketcetera.core.InternalID;
import org.marketcetera.core.MSymbol;
import org.marketcetera.core.FeedComponent.FeedStatus;
import org.marketcetera.photon.model.DBFIXMessageHistory;
import org.marketcetera.photon.model.Portfolio;
import org.marketcetera.quickfix.ConnectionConstants;
import org.marketcetera.quickfix.FIXDataDictionaryManager;
import org.marketcetera.quickfix.FIXMessageUtil;

import quickfix.Message;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.field.Side;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IPlatformRunnable {

	
	private static final String CONTEXT_FACTORY_NAME_DEFAULT = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
	private static final String CONNECTION_FACTORY_NAME_DEFAULT = "ConnectionFactory";
	private static final String INCOMING_TOPIC_NAME_DEFAULT = "oms-messages";
	private static final String OUTGOING_QUEUE_NAME_DEFAULT = "oms-commands";


	public static String MAIN_CONSOLE_LOGGER_NAME = "main.console.logger";
    private static Logger mainConsoleLogger = Logger.getLogger(MAIN_CONSOLE_LOGGER_NAME);
    private static IDFactory idFactory = new InMemoryIDFactory(777);
	private static OrderManager orderManager;
	private static JMSConnector jmsConnector;
	private static Portfolio rootPortfolio;
	
	private static DBFIXMessageHistory fixMessageHistory;

	public static final String PLUGIN_ID = "org.marketcetera.photon";
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		
		FIXDataDictionaryManager.loadDictionary(FIXDataDictionaryManager.FIX_4_2_BEGIN_STRING);
		
		fixMessageHistory = new DBFIXMessageHistory();
		Message aMessage = FIXMessageUtil.newExecutionReport(new InternalID("1234"), new InternalID("456"), "987", ExecTransType.STATUS,
				ExecType.PARTIAL_FILL, OrdStatus.PARTIALLY_FILLED, Side.BUY, new BigDecimal(1000), new BigDecimal("12.3"), new BigDecimal(500), 
				new BigDecimal("12.3"), new BigDecimal(500), new BigDecimal(500), new BigDecimal("12.3"), new MSymbol("IBM"));
		fixMessageHistory.addIncomingMessage(aMessage);

		jmsConnector = new JMSConnector();
        rootPortfolio = new Portfolio(null, "Main Portfolio");

		orderManager = new OrderManager(idFactory, rootPortfolio, fixMessageHistory);
		Display display = PlatformUI.createDisplay();
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IPlatformRunnable.EXIT_RESTART;
			}
			return IPlatformRunnable.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	public static Logger getMainConsoleLogger()
	{
		return mainConsoleLogger;
	}
	
	public static OrderManager getOrderManager()
	{
		return orderManager;
	}
	
	public static JMSConnector initJMSConnector() throws JMSException 
	{
        ScopedPreferenceStore preferences = new ScopedPreferenceStore(new ConfigurationScope(), Application.PLUGIN_ID);
        EclipseConfigData config = new EclipseConfigData(preferences);
		String incomingTopicNameString = config.get(ConnectionConstants.JMS_INCOMING_TOPIC_KEY, INCOMING_TOPIC_NAME_DEFAULT);
		String outgoingQueueNameString = config.get(ConnectionConstants.JMS_OUTGOING_QUEUE_KEY, OUTGOING_QUEUE_NAME_DEFAULT);
		String contextFactoryString = config.get(ConnectionConstants.JMS_CONTEXT_FACTORY_KEY, CONTEXT_FACTORY_NAME_DEFAULT);
		String jmsURLString = config.get(ConnectionConstants.JMS_URL_KEY, "");
		String jmsConnectionFactoryString = config.get(ConnectionConstants.JMS_CONNECTION_FACTORY_KEY, CONNECTION_FACTORY_NAME_DEFAULT);
        try {
				jmsConnector.init(
						incomingTopicNameString,
						outgoingQueueNameString,
						contextFactoryString,
						jmsURLString,
						jmsConnectionFactoryString
						);
				setTopicListener(orderManager.getMessageListener());


			return jmsConnector;
		} catch (JMSException e) {
			getMainConsoleLogger().error("Could not connect to JMS server {"
					+ incomingTopicNameString +", "
					+ outgoingQueueNameString +", "
					+ contextFactoryString +", "
					+ jmsURLString +", "
					+ jmsConnectionFactoryString +"}"
					, e);
			throw e;
		}
		
	}


	/**
	 * @return Returns the jmsStatus.
	 */
	public static FeedStatus getJMSStatus() {
		return jmsConnector.getFeedStatus();
	}


	public static void setTopicListener(MessageListener pJMSListener) {
		if (jmsConnector != null && getJMSStatus().equals(FeedStatus.AVAILABLE)){
			try {
				jmsConnector.setTopicListener(pJMSListener);
			} catch (JMSException e) {
				getMainConsoleLogger().error("Could not set topic listener");
			}
		}
		
	}

	public static void sendToQueue(Message message) throws JMSException {
		if (jmsConnector.getFeedStatus() == FeedStatus.AVAILABLE){
			jmsConnector.sendToQueue(message);
		} else {
			Application.getMainConsoleLogger().error("Could not send message to queue ");
		}
	}

	/**
	 * @return Returns the fixMessageHistory.
	 */
	public static DBFIXMessageHistory getFIXMessageHistory() {
		return fixMessageHistory;
	}

	public static JMSConnector getJMSConnector() {
		return jmsConnector;
	}

	public static Portfolio getRootPortfolio() {
		return rootPortfolio;
	}

	public static IDFactory getIDFactory() {
		return idFactory;
	}
}
