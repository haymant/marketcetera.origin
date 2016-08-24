package org.marketcetera.rpc.server;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.Validate;
import org.marketcetera.rpc.Messages;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

/* $License$ */

/**
 *
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
public class RpcServer
{
    /**
     * 
     *
     *
     * @throws Exception
     */
    @PostConstruct
    public synchronized void start()
            throws Exception
    {
        Validate.notNull(hostname);
        Validate.isTrue(port > 0 && port < 65536);
        Messages.SERVER_STARTING.info(this,
                                      hostname,
                                      String.valueOf(port));
        // TODO bind to host?
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
        for(BindableService serverServiceDefinition : serverServiceDefinitions) {
            serverBuilder.addService(serverServiceDefinition);
        }
        server = serverBuilder.build();
        server.start();
    }
    /**
     * 
     *
     *
     */
    @PreDestroy
    public synchronized void stop()
    {
        Messages.SERVER_STOPPING.info(this);
        if(server != null) {
            try {
                server.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
            server = null;
        }
    }
    /**
     * Get the port value.
     *
     * @return an <code>int</code> value
     */
    public int getPort()
    {
        return port;
    }
    /**
     * Sets the port value.
     *
     * @param inPort an <code>int</code> value
     */
    public void setPort(int inPort)
    {
        port = inPort;
    }
    /**
     * Get the hostname value.
     *
     * @return a <code>String</code> value
     */
    public String getHostname()
    {
        return hostname;
    }
    /**
     * Sets the hostname value.
     *
     * @param inHostname a <code>String</code> value
     */
    public void setHostname(String inHostname)
    {
        hostname = inHostname;
    }
    /**
     * Get the serverServiceDefinitions value.
     *
     * @return a <code>List<BindableService></code> value
     */
    public List<BindableService> getServerServiceDefinitions()
    {
        return serverServiceDefinitions;
    }
    /**
     * Sets the serverServiceDefinitions value.
     *
     * @param inServerServiceDefinitions a <code>List<BindableService></code> value
     */
    public void setServerServiceDefinitions(List<BindableService> inServerServiceDefinitions)
    {
        serverServiceDefinitions = inServerServiceDefinitions;
    }
    /**
     * 
     */
    private int port;
    /**
     * 
     */
    private String hostname;
    /**
     * 
     */
    private Server server;
    /**
     * 
     */
    private List<BindableService> serverServiceDefinitions = new ArrayList<>();
}
