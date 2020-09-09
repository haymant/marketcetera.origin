package org.marketcetera.brokers.service;

import java.time.LocalDateTime;

import org.marketcetera.cluster.AbstractCallableClusterTask;
import org.marketcetera.util.log.SLF4JLoggerProxy;
import org.marketcetera.util.time.DateService;

import quickfix.Session;
import quickfix.SessionID;

/**
 * Gets the session start from a given session.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
public class GetSessionStartTask
        extends AbstractCallableClusterTask<LocalDateTime>
{
    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public LocalDateTime call()
            throws Exception
    {
        SLF4JLoggerProxy.debug(this,
                               "Calling get session start for {} on {}",
                               sessionId,
                               getClusterService().getInstanceData());
        Session session = Session.lookupSession(sessionId);
        if(session == null) {
            return null;
        }
        return DateService.toLocalDateTime(session.getStartTime());
    }
    /**
     * Create a new GetSessionStartTask instance.
     *
     * @param inSessionId a <code>SessionID</code> value
     */
    public GetSessionStartTask(SessionID inSessionId)
    {
        sessionId = inSessionId;
    }
    /**
     * session id value
     */
    private SessionID sessionId;
    private static final long serialVersionUID = 7182143924518821961L;
}
