package org.marketcetera.core.ws.types;

import java.io.Serializable;

/**
 * @since 1.0.0
 * @version $Id: WrappableObject.java 82324 2012-04-09 20:56:08Z colin $
 */

/* $License$ */

public class WrappableObject
    implements Serializable,
               Comparable<WrappableObject>
{
    private static final long serialVersionUID=1L;


    private int mValue;


    public WrappableObject
        (int value)
    {
        setValue(value);
    }


    public void setValue
        (int value)
    {
        mValue=value;
    }

    public int getValue()
    {
        return mValue;
    }


    @Override
    public int compareTo
        (WrappableObject other)
    {
        return getValue()-other.getValue();
    }
    
    @Override
    public int hashCode()
    {
        return getValue();
    }

    @Override
    public boolean equals
        (Object other)
    {
        if (this==other) {
            return true;
        }
        if ((other==null) || !getClass().equals(other.getClass())) {
            return false;
        }
        WrappableObject o=(WrappableObject)other;
        return (getValue()==o.getValue());
    }

    @Override
    public String toString()
    {
        return "Value: "+getValue();
    }
}
