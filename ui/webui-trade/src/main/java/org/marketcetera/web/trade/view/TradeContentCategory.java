package org.marketcetera.web.trade.view;

import org.marketcetera.web.view.MenuContent;

import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar.Command;

/* $License$ */

/**
 * Provides the menu category for trade actions.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
public class TradeContentCategory
        implements MenuContent
{
    /* (non-Javadoc)
     * @see com.marketcetera.web.view.MenuContent#getCaption()
     */
    @Override
    public String getMenuCaption()
    {
        return "Trade";
    }
    /* (non-Javadoc)
     * @see com.marketcetera.web.view.MenuContent#getWeight()
     */
    @Override
    public int getWeight()
    {
        return 400;
    }
    /* (non-Javadoc)
     * @see com.marketcetera.web.view.MenuContent#getCategory()
     */
    @Override
    public MenuContent getCategory()
    {
        return null;
    }
    /* (non-Javadoc)
     * @see com.marketcetera.web.view.MenuContent#getCommand()
     */
    @Override
    public Command getCommand()
    {
        return null;
    }
    /* (non-Javadoc)
     * @see com.marketcetera.web.view.MenuContent#getIcon()
     */
    @Override
    public Resource getMenuIcon()
    {
        return null;
    }
    /**
     * trade menu category
     */
    public static final TradeContentCategory instance = new TradeContentCategory();
}
