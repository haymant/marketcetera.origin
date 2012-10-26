package org.marketcetera.core.event.beans;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.marketcetera.api.systemmodel.instruments.FutureExpirationMonth;
import org.marketcetera.core.LoggerConfiguration;
import org.marketcetera.core.trade.DeliveryType;
import org.marketcetera.core.trade.FutureImpl;
import org.marketcetera.core.trade.FutureType;
import org.marketcetera.core.trade.FutureUnderlyingAssetType;
import org.marketcetera.core.trade.StandardType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/* $License$ */

/**
 * Tests {@link org.marketcetera.core.event.beans.FutureBean}.
 *
 * @version $Id: FutureBeanTest.java 82331 2012-04-10 16:29:48Z colin $
 * @since 2.1.4
 */
public class FutureBeanTest
{
    /**
     * Run once before all tests.
     *
     * @throws Exception if an unexpected error occurs
     */
    @BeforeClass
    public static void once()
            throws Exception
    {
        LoggerConfiguration.logSetup();
    }
    /**
     * Tests {@link org.marketcetera.core.event.beans.FutureBean#copy(org.marketcetera.core.event.beans.FutureBean)}.
     *
     * @throws Exception if an unexpected error occurs
     */
    @Test
    public void copy()
            throws Exception
    {
        doCopyTest(new FutureBean());
    }
    /**
     * Tests the ability to serialize a <code>FutureBean</code> value. 
     *
     * @throws Exception if an unexpected error occurs
     */
    @Test
    public void serializationTest()
            throws Exception
    {
        FutureBean bean = new FutureBean();
        // Serialize to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        ObjectOutput out = new ObjectOutputStream(bos) ;
        out.writeObject(bean);
        out.close();
    }
    /**
     * Performs one copy test. 
     *
     * @param inBean a <code>FutureBean</code> value
     * @throws Exception if an unexpected error occurs
     */
    private void doCopyTest(FutureBean inBean)
            throws Exception
    {
        verifyFutureBean(inBean,
                         null,
                         null,
                         null,
                         null,
                         null,
                         null,
                         1);
        FutureBean newBean = FutureBean.copy(inBean);
        verifyFutureBean(newBean,
                         null,
                         null,
                         null,
                         null,
                         null,
                         null,
                         1);
        FutureImpl future = new FutureImpl("METC",
                                   FutureExpirationMonth.DECEMBER,
                                   2011);
        inBean.setInstrument(future);
        DeliveryType deliveryType = DeliveryType.PHYSICAL;
        inBean.setDeliveryType(deliveryType);
        String providerSymbol = "METC-Z12";
        inBean.setProviderSymbol(providerSymbol);
        StandardType standardType = StandardType.STANDARD;
        inBean.setStandardType(standardType);
        FutureType futureType = FutureType.COMMODITY;
        inBean.setFutureType(futureType);
        FutureUnderlyingAssetType futureUnderlyingAssetType = new FutureUnderlyingAssetType() {};
        inBean.setUnderlyingAssetType(futureUnderlyingAssetType);
        int contractSize = Integer.MAX_VALUE;
        inBean.setContractSize(contractSize);
        verifyFutureBean(inBean,
                         future,
                         deliveryType,
                         providerSymbol,
                         standardType,
                         futureType,
                         futureUnderlyingAssetType,
                         contractSize);
        newBean = FutureBean.copy(inBean);
        verifyFutureBean(inBean,
                         future,
                         deliveryType,
                         providerSymbol,
                         standardType,
                         futureType,
                         futureUnderlyingAssetType,
                         contractSize);
    }
    /**
     * Verifies the given <code>FutureBean</code> has the given expected attributes.
     *
     * @param inActualBean a <code>FutureBean</code> value
     * @param inExpectedInstrument a <code>Future</code> value
     * @param inExpectedDeliveryType a <code>DeliveryType</code> value
     * @param inExpectedProviderSymbol a <code>String</code> value
     * @param inExpectedStandardType a <code>StandardType</code> value
     * @param inExpectedFutureType a <code>FutureType</code> value
     * @param inExpectedFutureUnderlyingAssetType a <code>FutureUnderlyingAssetType</code> value
     * @param inExpectedContractSize an <code>int</code> value
     * @throws Exception if an unexpected error occurs
     */
    private void verifyFutureBean(FutureBean inActualBean,
                                  FutureImpl inExpectedInstrument,
                                  DeliveryType inExpectedDeliveryType,
                                  String inExpectedProviderSymbol,
                                  StandardType inExpectedStandardType,
                                  FutureType inExpectedFutureType,
                                  FutureUnderlyingAssetType inExpectedFutureUnderlyingAssetType,
                                  int inExpectedContractSize)
            throws Exception
    {
        assertNotNull(inActualBean.toString());
        assertEquals(inExpectedInstrument,
                     inActualBean.getInstrument());
        assertEquals(inExpectedDeliveryType,
                     inActualBean.getDeliveryType());
        assertEquals(inExpectedProviderSymbol,
                     inActualBean.getProviderSymbol());
        assertEquals(inExpectedStandardType,
                     inActualBean.getStandardType());
        assertEquals(inExpectedFutureType,
                     inActualBean.getFutureType());
        assertEquals(inExpectedFutureUnderlyingAssetType,
                     inActualBean.getUnderlyingAssetType());
        assertEquals(inExpectedContractSize,
                     inActualBean.getContractSize());
    }
}
