package org.marketcetera.client;

import java.math.BigDecimal;
import java.util.Date;
import javax.jws.WebService;
import javax.jws.WebParam;

import org.marketcetera.client.brokers.BrokersStatus;
import org.marketcetera.client.users.UserInfo;
import org.marketcetera.trade.MSymbol;
import org.marketcetera.trade.ReportBaseImpl;
import org.marketcetera.trade.UserID;
import org.marketcetera.util.misc.ClassVersion;
import org.marketcetera.util.ws.stateful.ClientContext;
import org.marketcetera.util.ws.stateful.ServiceBase;
import org.marketcetera.util.ws.wrappers.RemoteException;
import org.marketcetera.util.ws.wrappers.MapWrapper;

/**
 * The application's web services.
 *
 * @author tlerios@marketcetera.com
 * @since 1.0.0
 * @version $Id$
 */

/* $License$ */

@WebService(targetNamespace = "http://marketcetera.org/services")
@ClassVersion("$Id$")
public interface Service
    extends ServiceBase
{

    /**
     * The prefix of the topic on which the client receives server
     * replies.
     */

    public static final String REPLY_TOPIC_PREFIX=
        "ors-messages-"; //$NON-NLS-1$

    /**
     * The topic on which the client receives broker status
     * notifications.
     */

    public static final String BROKER_STATUS_TOPIC=
        "ors-broker-status"; //$NON-NLS-1$

    /**
     * The queue on which the client places orders for the server.
     */

    public static final String REQUEST_QUEUE=
        "ors-commands"; //$NON-NLS-1$

    /**
     * Returns the server's broker status to the client with the
     * given context.
     *
     * @param context The context.
     *
     * @return The status.
     *
     * @throws RemoteException Thrown if the operation cannot be
     * completed.
     */

    BrokersStatus getBrokersStatus
        (@WebParam(name= "context") ClientContext context)
        throws RemoteException;

    /**
     * Returns the information of the user with the given ID to the
     * client with the given context.
     *
     * @param context The context.
     * @param id The user ID.
     *
     * @return The information.
     *
     * @throws RemoteException Thrown if the operation cannot be
     * completed.
     */

    UserInfo getUserInfo
        (@WebParam(name= "context")ClientContext context,
         @WebParam(name= "userID")UserID id)
        throws RemoteException;

    /**
     * Returns all the reports (execution report and order cancel
     * rejects) generated and received by the server since the
     * supplied date to the client with the given context.
     *
     * @param context The context.
     * @param date The date, in UTC.
     *
     * @return The reports.
     *
     * @throws RemoteException Thrown if the operation cannot be
     * completed.
     */

    ReportBaseImpl[] getReportsSince
        (@WebParam(name= "context")ClientContext context,
         @WebParam(name= "date")Date date)
        throws RemoteException;

    /**
     * Returns the position of the supplied symbol based on reports,
     * generated and received up until the supplied date in UTC to the
     * client with the given context.
     *
     * @param context The context.
     * @param date The date, in UTC.
     * @param symbol The symbol.
     *
     * @return The position.
     *
     * @throws RemoteException Thrown if the operation cannot be
     * completed.
     */

    BigDecimal getPositionAsOf
        (@WebParam(name= "context")ClientContext context,
         @WebParam(name= "date")Date date,
         @WebParam(name= "symbol")MSymbol symbol)
        throws RemoteException;

    /**
     * Returns all the open positions based on reports, generated and
     * received up until the supplied date in UTC to the client with
     * the given context.
     *
     * @param context The context.
     * @param date The date, in UTC.
     *
     * @return The open positions.
     *
     * @throws RemoteException Thrown if the operation cannot be
     * completed.
     */

    MapWrapper<MSymbol,BigDecimal> getPositionsAsOf
        (@WebParam(name= "context")ClientContext context,
         @WebParam(name= "date")Date date)
        throws RemoteException;

    /**
     * Returns the next server order ID to the client with the given
     * context.
     *
     * @param context The context.
     *
     * @return The next order ID.
     *
     * @throws RemoteException Thrown if the operation cannot be
     * completed.
     */

    String getNextOrderID
        (@WebParam(name= "context")ClientContext context)
        throws RemoteException;

    /**
     * Sends a heartbeat to the server.
     *
     * @param context The context.
     *
     * @throws RemoteException Thrown if the operation cannot be
     * completed.
     */

    void heartbeat
        (@WebParam(name= "context")ClientContext context)
        throws RemoteException;
}
