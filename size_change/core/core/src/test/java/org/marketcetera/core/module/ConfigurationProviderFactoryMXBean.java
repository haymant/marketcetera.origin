package org.marketcetera.core.module;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.management.MXBean;

/* $License$ */
/**
 * Management interface for {@link ConfigurationProviderTestFactory}.
 * This interface is used for testing if the configuration provider
 * can be used to set default properties on a factory.
 *
 */
@MXBean(true)
@DisplayName("Management interface for configuration provider bean")
public interface ConfigurationProviderFactoryMXBean {
    @DisplayName("Maximum Portfolio size")
    BigDecimal getMaxLimit();

    @DisplayName("Maximum Portfolio size")
    void setMaxLimit(
            @DisplayName("Maximum Portfolio size")
            BigDecimal inMaxLimit);

    @DisplayName("Number of instances created")
    BigInteger getInstances();
}
