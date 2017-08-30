package org.marketcetera.clienttest;

import java.math.BigDecimal;

import org.marketcetera.core.PlatformServices;
import org.marketcetera.trade.Equity;
import org.marketcetera.trade.Factory;
import org.marketcetera.trade.OrderSingle;
import org.marketcetera.trade.OrderType;
import org.marketcetera.trade.Side;
import org.marketcetera.trade.TradeMessage;
import org.marketcetera.trade.TradeMessageListener;
import org.marketcetera.trade.client.TradingClient;
import org.marketcetera.trading.rpc.TradingRpcClientFactory;
import org.marketcetera.trading.rpc.TradingRpcClientParametersImpl;
import org.marketcetera.util.log.SLF4JLoggerProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/* $License$ */

/**
 * Demonstrates how to connect to MATP order services from an external application.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
@SpringBootApplication
@EnableAutoConfiguration
@SpringBootConfiguration
public class ClientTest
{
    /**
     * Main run method.
     *
     * @param inArgs a <code>String[]</code> value
     */
    public static void main(String[] inArgs)
    {
        SpringApplication.run(ClientTest.class,
                              inArgs);
        SLF4JLoggerProxy.info(ClientTest.class,
                              "Starting client test");
        try {
            ClientTest clientTest = ClientTest.instance;
            clientTest.runTest();
        } catch (Exception e) {
            PlatformServices.handleException(ClientTest.class,
                                             "Client Test Error",
                                             e);
        }
        SLF4JLoggerProxy.info(ClientTest.class,
                              "Ending client test");
    }
    /**
     * Run the client test.
     *
     * @throws Exception if an error occurs running the client test
     */
    private void runTest()
            throws Exception
    {
        try {
            TradingRpcClientParametersImpl params = new TradingRpcClientParametersImpl();
            params.setHostname(hostname);
            params.setPort(port);
            params.setUsername(username);
            params.setPassword(password);
            tradingClient = tradeClientFactory.create(params);
            tradingClient.start();
            SLF4JLoggerProxy.info(ClientTest.class,
                                  "Client connected to {}:{} as {}",
                                  hostname,
                                  port,
                                  username);
            TradeMessageListener tradeMessageListener = new TradeMessageListener() {
                @Override
                public void receiveTradeMessage(TradeMessage inTradeMessage)
                {
                    SLF4JLoggerProxy.info(ClientTest.this,
                                          "Received {}",
                                          inTradeMessage);
                }
            };
            tradingClient.addTradeMessageListener(tradeMessageListener);
            Factory factory = Factory.getInstance();
            OrderSingle testOrder = factory.createOrderSingle();
            testOrder.setInstrument(new Equity("METC"));
            testOrder.setOrderType(OrderType.Limit);
            testOrder.setQuantity(BigDecimal.TEN);
            testOrder.setPrice(BigDecimal.TEN);
            testOrder.setSide(Side.Buy);
            SLF4JLoggerProxy.info(ClientTest.class,
                                  "Sending {}",
                                  testOrder);
            tradingClient.sendOrder(testOrder);
            Thread.sleep(5000);
            tradingClient.removeTradeMessageListener(tradeMessageListener);
        } finally {
            if(tradingClient != null) {
                tradingClient.stop();
            }
        }
    }
    /**
     * Create a new ClientTest instance.
     */
    public ClientTest()
    {
        instance = this;
    }
    /**
     * Get the trading client factory value.
     *
     * @return a <code>TradingRpcClientFactory</code> value
     */
    @Bean
    public TradingRpcClientFactory getTradingClientFactory()
    {
        TradingRpcClientFactory tradingClientFactory = new TradingRpcClientFactory();
        return tradingClientFactory;
    }
    @Bean
    public static ClientTest getClientTest()
    {
        return new ClientTest();
    }
    private static ClientTest instance;
    /**
     * hostname value
     */
    @Value("${metc.client.hostname:127.0.0.1}")
    private String hostname;
    /**
     * username value
     */
    @Value("${metc.client.username:trader}")
    private String username;
    /**
     * password value
     */
    @Value("${metc.client.password:trader}")
    private String password;
    /**
     * port value
     */
    @Value("${metc.client.port:8998}")
    private int port;
    /**
     * provides access to trading client services
     */
    private TradingClient tradingClient;
    /**
     * creates {@link TradingClient} objects
     */
    @Autowired
    private TradingRpcClientFactory tradeClientFactory;
}
