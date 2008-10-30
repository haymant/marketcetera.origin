package org.marketcetera.strategy;

/* $License$ */

/**
 *
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id: $
 * @since $Release$
 */
public interface IStrategyExecutor
{
    public void execute()
        throws StrategyExecutionException;
    public void halt();
}