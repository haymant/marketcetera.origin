package org.marketcetera.quickfix;

import org.marketcetera.core.ClassVersion;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.Session;

/**
 * Marker class for objects that send out FIX messages
 * Essentially used for classes that need to be subclassed in unit tests
 * when we want to just capture the message instead of sending it out.
 * @author toli
 * @version $Id$
 */
@ClassVersion("$Id$")
public class FIXMessageSender
{
    /**
     * Send outgoing message.
     *
     * @param inMsg a <code>Message</code> value
     * @param targetID a <code>SessionID</code> value
     * @throws SessionNotFound if the given session cannot be found
     */
    public void sendOutgoingMessage(Message inMsg,
                                    SessionID targetID)
            throws SessionNotFound
    {
        Session.sendToTarget(inMsg, targetID);
    }
}
