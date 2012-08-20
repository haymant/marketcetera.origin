package org.marketcetera.core.systemmodel;

import org.marketcetera.api.attributes.ClassVersion;

/* $License$ */

/**
 * Constructs {@link Authority} objects.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id: AuthorityFactory.java 82315 2012-03-17 01:58:54Z colin $
 * @since $Release$
 */
@ClassVersion("$Id: AuthorityFactory.java 82315 2012-03-17 01:58:54Z colin $")
public interface AuthorityFactory
{
    /**
     * Creates an <code>Authority</code> with the given name.
     *
     * @param inAuthorityName a <code>String</code> value
     * @return an <code>Authority</code> value
     */
    public Authority create(String inAuthorityName);
    /**
     * Creates an <code>Authority</code> object.
     *
     * @return an <code>Authority</code> value
     */
    public Authority create();
}
