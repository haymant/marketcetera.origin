package org.marketcetera.algo;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.marketcetera.core.CoreException;
import org.marketcetera.core.Validator;
import org.marketcetera.util.log.I18NBoundMessage1P;
import org.marketcetera.util.log.I18NBoundMessage2P;
import org.marketcetera.util.misc.ClassVersion;

/* $License$ */

/**
 * Represents a bound {@link BrokerAlgoTagSpec} and value.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ClassVersion("$Id$")
public class BrokerAlgoTag
        implements Serializable
{
    /**
     * Create a new BrokerAlgoTag instance.
     */
    public BrokerAlgoTag() {}
    /**
     * Create a new BrokerAlgoTag instance.
     *
     * @param inTagSpec a <code>BrokerAlgoTagSpec</code> value
     */
    public BrokerAlgoTag(BrokerAlgoTagSpec inTagSpec)
    {
        setTagSpec(inTagSpec);
    }
    /**
     * Create a new BrokerAlgoTag instance.
     *
     * @param inTagSpec a <code>BrokerAlgoTagSpec</code> value
     * @param inValue a <code>String</code> value
     */
    public BrokerAlgoTag(BrokerAlgoTagSpec inTagSpec,
                         String inValue)
    {
        setTagSpec(inTagSpec);
        setValue(inValue);
    }
    /**
     * Get the tagSpec value.
     *
     * @return an <code>AlgoTagSpec</code> value
     */
    public BrokerAlgoTagSpec getTagSpec()
    {
        return tagSpec;
    }
    /**
     * Sets the tagSpec value.
     *
     * @param inTagSpec an <code>AlgoTagSpec</code> value
     */
    public final void setTagSpec(BrokerAlgoTagSpec inTagSpec)
    {
        tagSpec = inTagSpec;
    }
    /**
     * Get the value value.
     *
     * @return a <code>String</code> value
     */
    public String getValue()
    {
        return value;
    }
    /**
     * Sets the value value.
     *
     * @param inValue a <code>String</code> value
     */
    public final void setValue(String inValue)
    {
        value = StringUtils.trimToNull(inValue);
    }
    /**
     * Validates the tag spec and tag value.
     * 
     * @param inAlgoTag a <code>BrokerAlgoTag</code> value
     * @throws CoreException if a validation error occurs
     */
    public void validate(BrokerAlgoTag inAlgoTag)
    {
        String pattern = tagSpec.getPattern();
        if(pattern != null) {
            if(!Pattern.compile(pattern).matcher(value).matches()) {
                throw new CoreException(new I18NBoundMessage2P(Messages.ALGO_TAG_VALUE_PATTERN_MISMATCH,
                                                               tagSpec.getTag(),
                                                               value));
            }
        }
        if(tagSpec.getIsMandatory() && value == null) {
            throw new CoreException(new I18NBoundMessage1P(Messages.ALGO_TAG_VALUE_REQUIRED,
                                                           tagSpec.getTag()));
        }
        Validator<BrokerAlgoTag> tagValidator = tagSpec.getValidator();
        if(tagValidator != null) {
            tagValidator.validate(inAlgoTag);
        }
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BrokerAlgoTag [tagSpec=").append(tagSpec).append(", value=").append(value).append("]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return builder.toString();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(tagSpec).append(value).toHashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BrokerAlgoTag other = (BrokerAlgoTag) obj;
        return new EqualsBuilder().append(tagSpec,other.tagSpec).append(value,other.value).isEquals();
    }
    /**
     * represents the algo tag template
     */
    @XmlElement
    private BrokerAlgoTagSpec tagSpec;
    /**
     * contains the value to apply to the given tag spec
     */
    @XmlAttribute
    private String value;
    private static final long serialVersionUID = 3428966503023221067L;
}
