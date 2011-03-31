package org.marketcetera.server.service;

import org.marketcetera.trade.Order;
import org.marketcetera.util.misc.ClassVersion;

/* $License$ */

/**
 *
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
@ClassVersion("$Id$")
public interface OrderDestinationSelector
{
    public OrderDestination selectDestination(Order inOrder);
}
