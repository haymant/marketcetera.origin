package org.marketcetera.marketdata.service;

import java.util.Deque;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.marketcetera.core.PlatformServices;
import org.marketcetera.core.publisher.ISubscriber;
import org.marketcetera.event.Event;
import org.marketcetera.marketdata.Capability;
import org.marketcetera.marketdata.Content;
import org.marketcetera.marketdata.MarketDataListener;
import org.marketcetera.marketdata.MarketDataRequest;
import org.marketcetera.marketdata.MarketDataStatus;
import org.marketcetera.marketdata.MarketDataStatusListener;
import org.marketcetera.marketdata.NoMarketDataProvidersAvailable;
import org.marketcetera.marketdata.core.manager.MarketDataManagerModuleFactory;
import org.marketcetera.module.DataFlowID;
import org.marketcetera.module.DataRequest;
import org.marketcetera.module.ModuleManager;
import org.marketcetera.module.ModuleURN;
import org.marketcetera.modules.publisher.PublisherModuleFactory;
import org.marketcetera.persist.PageRequest;
import org.marketcetera.trade.Instrument;
import org.marketcetera.util.log.SLF4JLoggerProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/* $License$ */

/**
 * Provides market data services.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
@Service
public class MarketDataServiceImpl
        implements MarketDataService
{
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.MarketDataStatusPublisher#addMarketDataStatusListener(org.marketcetera.marketdata.MarketDataStatusListener)
     */
    @Override
    public void addMarketDataStatusListener(MarketDataStatusListener inMarketDataStatusListener)
    {
        throw new UnsupportedOperationException(); // TODO
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.MarketDataStatusPublisher#removeMarketDataStatusListener(org.marketcetera.marketdata.MarketDataStatusListener)
     */
    @Override
    public void removeMarketDataStatusListener(MarketDataStatusListener inMarketDataStatusListener)
    {
        throw new UnsupportedOperationException(); // TODO
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.MarketDataStatusBroadcaster#reportMarketDataStatus(org.marketcetera.marketdata.MarketDataStatus)
     */
    @Override
    public void reportMarketDataStatus(MarketDataStatus inMarketDataStatus)
    {
        throw new UnsupportedOperationException(); // TODO
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.service.MarketDataService#request(org.marketcetera.marketdata.MarketDataRequest, org.marketcetera.marketdata.MarketDataListener)
     */
    @Override
    public String request(MarketDataRequest inRequest,
                          MarketDataListener inMarketDataListener)
    {
        String provider = inRequest.getProvider();
        // TODO make sure request id is unique
        String requestId = inRequest.getRequestId();
        SLF4JLoggerProxy.debug(this,
                               "Requesting market data: {} from {}",
                               inRequest,
                               provider);
        if(provider == null) {
            SLF4JLoggerProxy.debug(this,
                                   "No provider requested, issuing request to all providers");
            boolean atLeastOne = false;
            for(ModuleURN providerUrn : moduleManager.getProviders()) {
                String providerType = providerUrn.providerType();
                String providerName = providerUrn.providerName();
                if(providerType.equals("mdata") && !providerName.equals(MarketDataManagerModuleFactory.PROVIDER_NAME)) {
                    for(ModuleURN instanceUrn : ModuleManager.getInstance().getModuleInstances(providerUrn)) {
                        try {
                            doDataRequest(inRequest,
                                          instanceUrn,
                                          inMarketDataListener,
                                          requestId);
                            atLeastOne = true;
                        } catch (Exception e) {
                            SLF4JLoggerProxy.warn(this,
                                                  "Unable to request market data from {}: {}",
                                                  instanceUrn,
                                                  ExceptionUtils.getRootCauseMessage(e));
                        }
                    }
                }
            }
            if(!atLeastOne) {
                throw new NoMarketDataProvidersAvailable(new IllegalArgumentException("No market data providers available for request " + requestId));
            }
        } else {
            ModuleURN sourceUrn = getInstanceUrn(provider);
            doDataRequest(inRequest,
                          sourceUrn,
                          inMarketDataListener,
                          requestId);
        }
        return inRequest.getRequestId();
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.service.MarketDataService#cancel(java.lang.String)
     */
    @Override
    public void cancel(String inRequestId)
    {
        SLF4JLoggerProxy.debug(this,
                               "Received a cancel request for {}",
                               inRequestId);
        RequestMetaData requestMetaData = requestsByRequestId.getIfPresent(inRequestId);
        requestsByRequestId.invalidate(inRequestId);
        if(requestMetaData == null) {
            throw new IllegalArgumentException("Unknown request: " + inRequestId);
        }
        requestMetaData.setIsActive(false);
        try {
            SLF4JLoggerProxy.debug(this,
                                   "Canceling market data request: {}",
                                   requestMetaData.getDataFlowId());
            moduleManager.cancel(requestMetaData.getDataFlowId());
        } catch (Exception e) {
            PlatformServices.handleException(this,
                                             "Cancel market data request",
                                             e);
        }
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.service.MarketDataService#getSnapshot(org.marketcetera.trade.Instrument, org.marketcetera.marketdata.Content, java.lang.String)
     */
    @Override
    public Deque<Event> getSnapshot(Instrument inInstrument,
                                    Content inContent,
                                    String inProvider)
    {
        throw new UnsupportedOperationException(); // TODO
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.service.MarketDataService#getSnapshotPage(org.marketcetera.trade.Instrument, org.marketcetera.marketdata.Content, java.lang.String, org.marketcetera.persist.PageRequest)
     */
    @Override
    public Deque<Event> getSnapshotPage(Instrument inInstrument,
                                        Content inContent,
                                        String inProvider,
                                        PageRequest inPageRequest)
    {
        throw new UnsupportedOperationException(); // TODO
    }
    /* (non-Javadoc)
     * @see org.marketcetera.marketdata.service.MarketDataService#getAvailableCapability()
     */
    @Override
    public Set<Capability> getAvailableCapability()
    {
        throw new UnsupportedOperationException(); // TODO
    }
    /**
     * Validate and start the object.
     */
    @PostConstruct
    public void start()
    {
        
    }
    /**
     * Get the instance URN for the given market data provider name.
     *
     * @param inProviderName a <code>String</code> value
     * @return a <code>ModuleURN</code> value
     */
    private ModuleURN getInstanceUrn(String inProviderName)
    {
        ModuleURN instanceUrn = instanceUrnsByProviderName.getIfPresent(inProviderName);
        if(instanceUrn == null) {
            // this will be our guess in case we don't find something
            instanceUrn = new ModuleURN("metc:mdata:" + inProviderName+":single");
            for(ModuleURN moduleUrn : ModuleManager.getInstance().getProviders()) {
                String providerType = moduleUrn.providerType();
                if(providerType.equals("mdata") && moduleUrn.providerName().equals(inProviderName)) {
                    instanceUrnsByProviderName.put(inProviderName,
                                                   moduleUrn);
                    instanceUrn = moduleUrn;
                    break;
                }
            }
        }
        return instanceUrn;
    }
    /**
     * Execute the given market data request.
     *
     * @param inMarketDataRequest a <code>MarketDataRequest</code> value
     * @param inSourceUrn a <code>ModuleURN</code> value
     * @param inListener an <code>ISubscriber</code> value
     * @param inRequestId a <code>long</code> value
     */
    private void doDataRequest(MarketDataRequest inMarketDataRequest,
                               ModuleURN inSourceUrn,
                               MarketDataListener inListener,
                               String inRequestId)
    {
        DataRequest sourceRequest = new DataRequest(inSourceUrn,
                                                    inMarketDataRequest);
        RequestMetaData requestMetaData = new RequestMetaData(inListener);
        DataRequest targetRequest = new DataRequest(createPublisherModule(requestMetaData));
        DataFlowID dataFlowId = moduleManager.createDataFlow(new DataRequest[] { sourceRequest,targetRequest });
        requestMetaData.setDataFlowId(dataFlowId);
        requestsByRequestId.put(inRequestId,
                                requestMetaData);
        SLF4JLoggerProxy.debug(this,
                               "Submitting {} to {}: {}",
                               inMarketDataRequest,
                               inSourceUrn,
                               dataFlowId);
    }
    /**
     * Create a publisher module.
     *
     * @return a <code>ModuleURN</code> value
     */
    private ModuleURN createPublisherModule(final RequestMetaData inRequestMetaData)
    {
        ModuleURN publisherUrn = moduleManager.createModule(PublisherModuleFactory.PROVIDER_URN,
                                                            new ISubscriber(){
            @Override
            public boolean isInteresting(Object inData)
            {
                return true;
            }
            @Override
            public void publishTo(Object inData)
            {
                inRequestMetaData.doPublish(inData);
            }}
        );
        return publisherUrn;
    }
    /**
     * Holds data about the market data request.
     *
     * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
     * @version $Id$
     * @since $Release$
     */
    private static class RequestMetaData
    {
        /**
         * Get the dataFlowId value.
         *
         * @return a <code>DataFlowID</code> value
         */
        private DataFlowID getDataFlowId()
        {
            return dataFlowId;
        }
        /**
         * Sets the dataFlowId value.
         *
         * @param inDataFlowId a <code>DataFlowID</code> value
         */
        private void setDataFlowId(DataFlowID inDataFlowId)
        {
            dataFlowId = inDataFlowId;
        }
        /**
         * Publish the given data.
         *
         * @param inData an <code>Object</code> value
         */
        private void doPublish(Object inData)
        {
            if(isActive()) {
                marketDataListener.receiveMarketData((Event)inData);
            }
        }
        /**
         * Get the isActive value.
         *
         * @return a <code>boolean</code> value
         */
        private boolean isActive()
        {
            return isActive;
        }
        /**
         * Sets the isActive value.
         *
         * @param inIsActive a <code>boolean</code> value
         */
        private void setIsActive(boolean inIsActive)
        {
            isActive = inIsActive;
        }
        /**
         * Create a new RequestMetaData instance.
         *
         * @param inListener a <code>MarketDataListener</code> value
         */
        private RequestMetaData(MarketDataListener inListener)
        {
            marketDataListener = inListener;
            isActive = true;
        }
        /**
         * indicates if the listener is active or not
         */
        private volatile boolean isActive;
        /**
         * listener value
         */
        private final MarketDataListener marketDataListener;
        /**
         * data flow id value
         */
        private DataFlowID dataFlowId;
    }
    /**
     * request data by request id
     */
    private Cache<String,RequestMetaData> requestsByRequestId = CacheBuilder.newBuilder().build();
    /**
     * provides access to module services
     */
    @Autowired
    private ModuleManager moduleManager;
    /**
     * holds market data provider instances by provider name
     */
    private final Cache<String,ModuleURN> instanceUrnsByProviderName = CacheBuilder.newBuilder().build();
}
