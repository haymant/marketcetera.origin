package org.marketcetera.module;

import org.marketcetera.util.misc.ClassVersion;

import javax.management.MXBean;
import java.util.List;

/* $License$ */
/**
 * Enables remoting of {@link ModuleManager module manager}.
 * All the operations accept primitive java types and strings as parameters,
 *  so that they can be invoked from simple JMX clients like jconsole.
 *
 * The following
 * APIs accept parameters in special syntax so that they can be translated
 * to complex data types that the underlying module manager API accepts.
 *
 * <h3>Create Module Instance</h3>
 * This parameter syntax applies to the second parameter to the API
 * {@link #createModule(String, String)}.
 * <p>
 * The parameter list can have a comma separated list of individual parameters.
 * The framework will convert the supplied parameters from string to the
 * parameter type specified by the factory via its
 * {@link ModuleFactory#getParameterTypes()} method. The conversion from
 * string to the appropriate type is supported for following types.
 * <ol>
 *  <li>Java Primitive Types</li>
 *  <li>Strings</li>
 *  <li>BigDecimal</li>
 *  <li>BigInteger</li>
 *  <li>File</li>
 *  <li>URL</li>
 *  <li>ModuleURN</li>
 * </ol>
 * If the factory accepts a parameter of type that is not included in
 * the list above, its instances cannot be created via the JMX API.
 * Do note that no mechanism is available to escape a comma character such
 * that its not treated as a delimiter between two parameters. ie. none
 * of the parameters can include a comma in their value.
 * <p>
 * Here are some examples of strings that can be used to supply parameter values
 * when invoking the create module instance API.
 *
 * <ul>
 *  <li>Example 1:</li>
 *  <ul>
 *      <li>Parameter Types:ModuleURN, URL, String, BigDecimal</li>
 *      <li>Parameter Value:
 *          <code>"metc:surface:color:red,http://red.com,username,12.43"</code>
 *      </li>
 *  </ul>
 *  <li>Example 2:</li>
 *  <ul>
 *      <li>Parameter Types:File, boolean, double, BigInteger</li>
 *      <li>Parameter Value:<code>"c:\\mydir,true,345.643,83723849"</code></li>
 *  </ul>
 * </ul>
 *
 * <h3>Create Data Flows</h3>
 * This parameter syntax applies to the first parameter of APIs
 * {@link #createDataFlow(String)} &
 * {@link #createDataFlow(String, boolean)}.
 * <p>
 * The API accepts a series of data requests. Each data request consists of
 * a {@link ModuleURN} and an optional string parameter.
 * Individual data requests are delimited by a '^' character.
 * If a data request needs to include the '^', character it can be escaped
 * by including it twice, like so '^^', so that its not interpreted
 * as a delimiter between data requests.
 * <p>
 * Within each data request, the module URN and the parameter are
 * separated by ';' character. The first entry is always
 * interpreted as the <code>ModuleURN</code>. The second entry is interpreted
 * as a string request parameter. The string parameter value can contain
 * ';' character and it will not interfere with data request parsing as
 * it only looks for the first ';' character in the string and interprets the
 * rest of the string as the string parameter value.
 * <p>
 * Here are some examples of strings that can be used when setting up data
 * flows.
 * <ul>
 *  <li>Example 1.</li>
 *  <ul>
 *      <li><code>"metc:mdata;symbol=ibm^metc:strategy::vegas^metc:sink"</code></li>
 *      <li>Sets up a data flow between any market data module that generates
 *          market data for symbol 'ibm', pipes that data to a strategy named
 *          'vegas' and pipes its output to the sink module.</li>
 *  </ul>
 *  <li>Example 2.</li>
 *  <ul>
 *      <li><code>"metc:mdata:opentick;symbol=java^metc:strategy::charity^metc:server"</code></li>
 *      <li>Sets up a data flow between opentick market data module, that
 *      generates market data for symbol 'java', pipes that data to a
 *      strategy called 'charity' and pipes the trades generated by it to
 *      the system client module that sends it to the server.</li>
 *  </ul>
 * <li>Example 3.</li>
 *  <ul>
 *      <li><code>"metc:mdata;symbol=aapl,goog^metc:cep:esper;select * from Bid^metc:strategy::buy;12^^32^^43"</code></li>
 *      <li>Sets up a data flow between any market data module that generates
 *          data for symbols, aapl & goog, pipes that data into the esper module, that
 *          runs the cep query 'select * from Bid' and pipes its output to
 *          a strategy module 'buy' that accepts a string parameter '12^32^43'.
 *      </li> 
 *  </ul>
 * </ul>
 *
 *  
 *
 * @author anshul@marketcetera.com
 * @version $Id$
 * @since 1.0.0
 */
@ClassVersion("$Id$")  //$NON-NLS-1$
@MXBean(true)
@DisplayName("Module Framework Operations")
public interface ModuleManagerMXBean {
    /**
     * Returns a list of URNs of available module providers.
     *
     * @return the list of URNs of available module providers.
     */
    @DisplayName("Fetches the URNs of all the module providers available in the system")
    List<String> getProviders();

    /**
     * Returns a list of URNs of all module instances.
     *
     * @return a list of URNs of all module instances.
     */
    @DisplayName("Fetches the URNs of all the module instances available in the system")
    List<String> getInstances();
    /**
     * Returns detailed information on a provider, given its URN.
     *
     * @param providerURN the provider URN
     *
     * @return the provider details.
     *
     * @throws RuntimeException if a provider with the supplied
     * URN does not exist. OR if the supplied provider URN is not a
     * valid URN.
     */
    @DisplayName("Fetches Provider Details")
    ProviderInfo getProviderInfo(
            @DisplayName("Provider URN")
            String providerURN) throws RuntimeException;

    /**
     * Returns the module instances of the module provider, given its URN.
     *
     * @param providerURN the providerURN whose module
     * instances are requested. If null, all module instances
     * are returned.
     *
     * @return the list or URNs for all the modules
     *
     * @throws RuntimeException if a provider with the supplied
     * URN does not exist. OR if the supplied provider URN is not a
     * valid URN.
     */
    @DisplayName("Fetches the URNs of all the module instances registered with the Framework")
    List<String> getModuleInstances(
            @DisplayName("Provider URN, to return its instances only, null otherwise")
            String providerURN)
            throws RuntimeException;

    /**
     * Creates a module instance. An attempt to create a module instance
     * for a provider that only supports singleton instances will fail.
     * All singleton instances are created when ModuleManager is
     * initialized.
     * <p>
     * This method can only instantiate modules whose creation requires
     * only those parameter types as supported by
     * {@link org.marketcetera.module.StringToTypeConverter}. If an
     * attempt is made to create a module supplying string value for
     * an unsupported type, the module creation will fail with a type
     * mismatch error.
     * <p>
     * Parameters of any type can be used to instantiate modules via
     * {@link ModuleManager#createModule(ModuleURN, Object[])}.
     * However, this API is only available for local invocation as
     * JMX doesn't support remoting of any random data type.
     *
     *
     * @param providerURN The provider URN. The value supplied should match
     * the value returned by a module factory's
     * {@link org.marketcetera.module.ModuleFactory#getProviderURN()} that
     * is available in the system.
     * @param parameterList the comma separated list of parameters that
     * are needed to instantiate the module. The string parameters are
     * converted to object types based on type values returned by
     * {@link ModuleFactory#getParameterTypes()}. If any of the types
     * returned by {@link ModuleFactory#getParameterTypes()} are not
     * supported by {@link org.marketcetera.module.StringToTypeConverter},
     * this method will fail.
     *
     * @return the instantiated module's URN
     *
     * @throws RuntimeException if there were errors creating
     * the module
     *
     * @see #getProviderInfo(String) 
     */
    @DisplayName("Creates a new module instance")
    String createModule(
            @DisplayName("Provider URN")
            String providerURN,
            @DisplayName("The comma separated list of parameters to create new instance")
            String parameterList)
            throws RuntimeException;

    /**
     * Deletes the module identified by the supplied module URN.
     * The module is stopped if its already running.
     * Singleton instances of a module cannot be deleted.
     *
     * @param inModuleURN the module URN, that uniquely identifies
     * the module being deleted.
     *
     * @throws RuntimeException if a module with the supplied
     * URN cannot be deleted OR if the supplied module URN is not
     * a valid URN. OR if the module matching the URN was not found.
     */
    @DisplayName("Delete a module instance")
    void deleteModule(
            @DisplayName("The module instance URN")
            String inModuleURN)
            throws RuntimeException;

    /**
     * Returns detailed information on the module having the supplied
     * module URN.
     *
     * @param inModuleURN the module URN
     *
     * @return the detailed module information.
     *
     * @throws RuntimeException if a module with the supplied
     * URN was not found OR if the supplied URN was invalid. OR if there
     * were errors creating module's ObjectName
     */
    @DisplayName("Fetches the module instance details")
    ModuleInfo getModuleInfo(
            @DisplayName("The module instance URN")
            String inModuleURN)
            throws RuntimeException;

    /**
     * Starts the module instance
     *
     * @param inModuleURN the module instance URN uniquely identifying
     * the module that needs to be started.
     *
     * @throws RuntimeException if there were errors starting the module.
     */
    @DisplayName("Starts a module instance")
    void start(
            @DisplayName("The module instance URN")
            String inModuleURN) throws RuntimeException;

    /**
     * Stops a module instance. Do note that stopping a module stops
     * all the data flows that this module initiated and is participating
     * in.
     *
     * @param inModuleURN the module instance URN uniquely identifying
     * the module that needs to be stopped.
     *
     * @throws RuntimeException if a module with the supplied
     * URN was not found OR if the supplied module URN is invalid.
     */
    @DisplayName("Stops a module instance")
    void stop(
            @DisplayName("The module instance URN")
            String inModuleURN) throws RuntimeException;

    /**
     * Creates a requested connection between the modules identified by
     * the supplied requests. Each data request should uniquely identify
     * a module via its URN attribute. Its an error if none or
     * multiple modules match the URN.
     *
     * For each matched module, a request is initiated supplying it the
     * data request.
     *
     * The system will automatically append the sink module to the
     * data flow if the last module identified by the request is
     * capable of emitting data and if the sink has not been already
     * specified as the last module in the pipeline.
     *
     * Invoking this method is the same as invoking
     * <code>createDataFlow(requests,true);</code>
     *
     * @param inRequests the request instances
     *
     * @return the ID identifying the data flow.
     *
     * @throws RuntimeException if any of the requested modules could
     * not be found, or instantiated or configured. Or if any of the
     * modules were not capable of emitting or receiving data as
     * requested. Or if any of the modules didn't understand the
     * request parameters or were unable to emit data as requested.
     *
     */
    @DisplayName("Creates a new data flow, automatically appending the sink module")
    DataFlowID createDataFlow(
            @DisplayName("The series of requests specifying the flow")
            String inRequests) throws RuntimeException;

    /**
     * Creates a requested connection between the modules identified by
     * the supplied requests. Each data request should uniquely identify
     * a module via its URN attribute. Its an error if none or
     * multiple modules match the URN.
     *
     * For each matched module, a request is initiated supplying it the
     * data request.
     *
     * Each of the modules specified in the request should already be
     * started for the request to succeed. This request will fail
     * if any of the modules specified in the request are not started
     * and are not
     * {@link Module#isAutoStart() auto-start}.
     *
     * @param inRequests the request instances
     * @param inAppendSink if the sink module should be automatically
     * appended to the tail end of the data flow.
     *
     * @return the ID identifying the data flow.
     *
     * @throws RuntimeException if any of the requested modules could
     * not be found, or instantiated or configured. Or if any of the
     * modules were not capable of emitting or receiving data as
     * requested. Or if any of the modules didn't understand the
     * request parameters or were unable to emit data as requested.
     *
     */
    @DisplayName("Creates a data flow")
    DataFlowID createDataFlow(
            @DisplayName("The series of requests specifying the data flow")
            String inRequests,
            @DisplayName("If the sink module should be automatically appended to the flow")
            boolean inAppendSink)
            throws RuntimeException;

    /**
     * Cancels the data flow identified by the supplied data flow ID.
     * Do note that data flows that have been initiated by
     * {@link #createDataFlow(String, boolean)} can be canceled by this
     * method.
     *
     * Specifically, data flows created by modules via
     * {@link DataFlowSupport#createDataFlow(DataRequest[])}
     * cannot be canceled by this method. They can only be canceled by the
     * module that initiated the data flow request.
     *
     * @param inFlowID the data flow ID.
     *
     * @throws RuntimeException if the data flow, specified by
     * the ID, could not be found.
     */
    @DisplayName("Cancels an active data flow")
    void cancel(
            @DisplayName("The data flow ID")
            String inFlowID)
            throws RuntimeException;

    /**
     * Returns all the data flows.
     *
     * @param inIncludeModuleCreated if the data flows created by
     * the module should be included in the returned list.
     *
     * @return the list of IDs of all data flows in the system.
     */
    @DisplayName("Gets the list of IDs of all active data flows")
    List<DataFlowID> getDataFlows(
            @DisplayName("If data flows created by the modules should be included")
            boolean inIncludeModuleCreated);

    /**
     * Returns details of data flow given the data flow ID.
     *
     * @param inFlowID the data flow ID
     *
     * @return the data flow details
     *
     * @throws RuntimeException if the data flow, specified by
     * the ID, could not be found.
     */
    @DisplayName("Fetches an active data flow's details")
    DataFlowInfo getDataFlowInfo(
            @DisplayName("The data flow ID")
            String inFlowID)
            throws RuntimeException;

    /**
     * Refreshes the set of module providers. Any new module provider
     * jars that have been made available in the class path will be
     * discovered and processed as a result.
     *
     * The existing module providers will remain unchanged.
     *
     * @throws RuntimeException if there were errors initializing
     * newly discovered factories.
     */
    @DisplayName("Refreshes the providers list. Checks to see if any new provider implementations were added")
    void refresh() throws RuntimeException;

    /**
     * Returns the historical record of data flows that are not active
     * any more. The maximum size of the returned array is determined
     * by the current value of {@link #getMaxFlowHistory()}
     *
     * @return historical record of data flows that are not active
     * any more.
     */
    @DisplayName("Data flows that are not active any more")
    List<DataFlowInfo> getDataFlowHistory();

    /**
     * The maximum number of data flow records to maintain
     * in the data flow history.
     *
     * @return the maximum number of historical data flow
     * records.
     */
    @DisplayName("Maximum number of data flow records to retain in data flow history")
    int getMaxFlowHistory();

    /**
     * Sets the maximum number of data flow records to maintain
     * in the data flow history.
     * 
     * If the value is reset to a value lower than the current value,
     * the older history records are pruned to bring down the size
     * of the historical records to the new value.
     * 
     * @param inMaxFlowHistory the maximum number of data flow
     * records.
     */
    @DisplayName("Maximum number of data flow records to retain in data flow history")
    void setMaxFlowHistory(
            @DisplayName("Maximum number of data flow records to retain in data flow history")
            int inMaxFlowHistory);
}
