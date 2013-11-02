package org.marketcetera.ors.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.TemporalType;

import org.junit.Test;
import org.marketcetera.event.HasFIXMessage;
import org.marketcetera.ors.PersistTestBase;
import org.marketcetera.trade.*;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.Side;

import quickfix.field.*;

/* $License$ */
/**
 * Verifies {@link ExecutionReportSummary}.
 *
 * @author anshul@marketcetera.com
 * @version $Id$
 * @since 1.0.0
 */
public abstract class ExecReportSummaryTestBase<I extends Instrument>
        extends PersistTestBase
{
    /**
     * Verify empty actor/viewer.
     *
     * @throws Exception if there were errors
     */
    @Test
    public void emptyViewer()
            throws Exception
    {
        reportHistoryServices.save(createExecReport("o1",
                                                    null,
                                                    getInstrument(),
                                                    Side.Buy,
                                                    OrderStatus.PartiallyFilled,
                                                    BigDecimal.TEN,
                                                    BigDecimal.TEN,
                                                    BigDecimal.ONE,
                                                    BigDecimal.ONE,
                                                    BROKER,
                                                    null,
                                                    null));
        reportHistoryServices.save(createExecReport("o1",
                                                    null,
                                                    getInstrument(),
                                                    Side.Buy,
                                                    OrderStatus.PartiallyFilled,
                                                    BigDecimal.TEN,
                                                    BigDecimal.TEN,
                                                    BigDecimal.ONE,
                                                    BigDecimal.ONE));
        List<ExecutionReportSummary> summary = reportService.findAllExecutionReportSummary();
        assertEquals(2,
                     summary.size());
        assertNull(summary.get(0).getViewer());
        assertNull(summary.get(0).getViewerID());
        assertEquals(viewer.getName(),
                     summary.get(1).getViewer().getName());
        assertEquals(viewerID,
                     summary.get(1).getViewerID());
    }
    /**
     * Verifies no summaries are saved for cancel rejects.
     *
     * @throws Exception if there were errors.
     */
    @Test
    public void rejectNotSaved()
            throws Exception
    {
        OrderCancelReject reject = createCancelReject();
        reportHistoryServices.save(reject);
        //report got saved
        assertEquals(1, reportService.findAllPersistentReport().size());
        //but the summary didn't
        List<ExecutionReportSummary> allErSummary = reportService.findAllExecutionReportSummary();
        assertEquals(0, allErSummary.size());
    }

    /**
     * Verifies that summary is saved for an execution report and that it's
     * saved with expected values for all attributes.
     * <p>
     * Also verifies that the root ID is correctly fetched an assigned
     * when a chain of execution reports are received.
     * <p>
     * Also verifies that sorting the results by ID works.
     *
     * @throws Exception if there were errors.
     */
    @Test
    public void execReportSave()
            throws Exception
    {
        String orderID1 = "ord1";
        I instrument = getInstrument();
        // A report with null orig ID. The root ID should be set to orderID1
        ExecutionReport report1 = createExecReport(orderID1,
                                                   null,
                                                   instrument,
                                                   Side.Buy,
                                                   OrderStatus.PartiallyFilled,
                                                   BigDecimal.TEN,
                                                   BigDecimal.TEN,
                                                   BigDecimal.ONE,
                                                   BigDecimal.ONE);
        reportHistoryServices.save(report1);
        // report got saved
        List<PersistentReport> reports = reportService.findAllPersistentReport();
        assertEquals(1,
                     reports.size());
        //and so did the summary
        List<ExecutionReportSummary> execReports = reportService.findAllExecutionReportSummary();
//        query.setEntityOrder(MultiExecReportSummary.BY_ID); // TODO sort order
        assertEquals(1,
                     execReports.size());
        assertSummary(execReports.get(0),
                      report1.getAveragePrice(),
                      report1.getCumulativeQuantity(),
                      report1.getLastPrice(),
                      report1.getLastQuantity(),
                      report1.getOrderID(),
                      report1.getOrderStatus(),
                      null,
                      reports.get(0),
                      report1.getOrderID(),
                      report1.getSendingTime(),
                      report1.getSide(),
                      instrument);
        String orderID2 = "ord2";
        // A report with orig ID set to previous order. The root ID should be set to orderID1
        ExecutionReport report2 = createExecReport(orderID2,
                                                   orderID1,
                                                   instrument,
                                                   Side.Buy,
                                                   OrderStatus.PartiallyFilled,
                                                   BigDecimal.TEN,
                                                   BigDecimal.TEN,
                                                   BigDecimal.ONE,
                                                   BigDecimal.ONE);
        reportHistoryServices.save(report2);
        reports = reportService.findAllPersistentReport();
        //report got saved
        assertEquals(2,
                     reports.size());
        //and so did the summary
        execReports = reportService.findAllExecutionReportSummary();
        assertEquals(2, execReports.size());
        assertSummary(execReports.get(1),
                      report2.getAveragePrice(),
                      report2.getCumulativeQuantity(),
                      report2.getLastPrice(),
                      report2.getLastQuantity(),
                      report2.getOrderID(),
                      report2.getOrderStatus(),
                      report2.getOriginalOrderID(),
                      reports.get(1),
                      report1.getOrderID(),
                      report2.getSendingTime(),
                      report2.getSide(),
                      instrument);
        String orderID3 = "ord3";
        // A report with orig ID set to previous order. The root ID should be set to orderID1
        ExecutionReport report3 = createExecReport(orderID3,
                                                   orderID2,
                                                   instrument,
                                                   Side.Buy,
                                                   OrderStatus.PartiallyFilled,
                                                   BigDecimal.TEN,
                                                   BigDecimal.TEN,
                                                   BigDecimal.ONE,
                                                   BigDecimal.ONE);
        reportHistoryServices.save(report3);
        reports = reportService.findAllPersistentReport();
        //report3 got saved
        assertEquals(3,
                     reports.size());
        //and so did the summary
        execReports = reportService.findAllExecutionReportSummary();
        assertEquals(3,
                     execReports.size());
        assertSummary(execReports.get(2),
                      report3.getAveragePrice(),
                      report3.getCumulativeQuantity(),
                      report3.getLastPrice(),
                      report3.getLastQuantity(),
                      report3.getOrderID(),
                      report3.getOrderStatus(),
                      report3.getOriginalOrderID(),
                      reports.get(2),
                      report1.getOrderID(),
                      report3.getSendingTime(),
                      report3.getSide(),
                      instrument);
    }
    /**
     * Verifies behavior when the report has original order ID value
     * set but no order corresponding to that could be found in the system.
     *
     * @throws Exception if there were errors.
     */
    @Test
    public void execReportOrigOrderNotPresent()
            throws Exception
    {
        //Create a report with orig orderID value such that no
        //record of an exec report with that order ID value exists
        I instrument = getInstrument();
        ExecutionReport report = createExecReport("ord1",
                                                  "ord2",
                                                  instrument,
                                                  Side.Buy,
                                                  OrderStatus.Replaced,
                                                  BigDecimal.TEN,
                                                  BigDecimal.TEN,
                                                  BigDecimal.ONE,
                                                  BigDecimal.ONE);
        reportHistoryServices.save(report);
        //report got saved
        List<PersistentReport> reports = reportService.findAllPersistentReport();
        assertEquals(1, reports.size());
        //and so did the summary
        List<ExecutionReportSummary> execReports = reportService.findAllExecutionReportSummary();
        assertEquals(1, execReports.size());
        assertSummary(execReports.get(0),
                      report.getAveragePrice(),
                      report.getCumulativeQuantity(),
                      report.getLastPrice(),
                      report.getLastQuantity(),
                      report.getOrderID(),
                      report.getOrderStatus(),
                      report.getOriginalOrderID(),
                      reports.get(0),
                      report.getOrderID(),
                      report.getSendingTime(),
                      report.getSide(),
                      instrument);
    }
    /**
     * Verifies that null avg px values cannot be persisted.
     *
     * @throws Exception if an unexpected error occurs
     */
    @Test
    public void nullAvgPriceFail()
            throws Exception
    {
        final ExecutionReport report = removeField(createDummyExecReport(),
                                                   AvgPx.FIELD);
        assertNull(report.getAveragePrice());
        nonNullCVCheck("avgPrice",
                       new Callable<Object>() {
            public Object call()
                    throws Exception
            {
                reportHistoryServices.save(report);
                return null;
            }
        });
    }
    /**
     * Verifies that null cum qty values cannot be persisted.
     *
     * @throws Exception if an unexpected error occurs
     */
    @Test
    public void nullCumQtyFail()
            throws Exception
    {
        final ExecutionReport report = removeField(createDummyExecReport(),
                                                   CumQty.FIELD);
        assertNull(report.getCumulativeQuantity());
        nonNullCVCheck("cumQuantity",
                       new Callable<Object>() {
            public Object call()
                    throws Exception
            {
                reportHistoryServices.save(report);
                return null;
            }
        });
    }
    /**
     * Verifies that null orderID values cannot be persisted.
     *
     * @throws Exception if an unexpected error occurs
     */
    @Test
    public void nullOrderIDFail()
            throws Exception
    {
        final ExecutionReport report = removeField(createDummyExecReport(),
                                                   ClOrdID.FIELD);
        assertNull(report.getOrderID());
        nonNullCVCheck("mOrderID",
                       new Callable<Object>() {
            public Object call()
                    throws Exception
            {
                reportHistoryServices.save(report);
                return null;
            }
        });
    }
    /**
     * Verifies that null order status values cannot be persisted.
     *
     * @throws Exception if an unexpected error occurs
     */
    @Test
    public void nullOrderStatusFail()
            throws Exception
    {
        final ExecutionReport report = removeField(createDummyExecReport(),
                                                   OrdStatus.FIELD);
        assertNull(report.getOrderStatus());
        nonNullCVCheck("orderStatus",
                       new Callable<Object>() {
            public Object call()
                    throws Exception
            {
                reportHistoryServices.save(report);
                return null;
            }
        });
    }
    /**
     * Verifies that null sending time values cannot be persisted.
     *
     * @throws Exception if an unexpected error occurs
     */
    @Test
    public void nullSendingTimeFail()
            throws Exception
    {
        final ExecutionReport report = (ExecutionReport)removeSendingTime(createDummyExecReport());
        nonNullCVCheck("sendingTime",
                       new Callable<Object>() {
            public Object call()
                    throws Exception
            {
                reportHistoryServices.save(report);
                return null;
            }
        });
    }
    /**
     * Verifies that null side values cannot be persisted.
     *
     * @throws Exception if an unexpected error occurs
     */
    @Test
    public void nullSideFail()
            throws Exception
    {
        final ExecutionReport report = removeField(createDummyExecReport(),
                                                   quickfix.field.Side.FIELD);
        assertNull(report.getSide());
        nonNullCVCheck("side",
                       new Callable<Object>() {
            public Object call()
                    throws Exception
            {
                reportHistoryServices.save(report);
                return null;
            }
        });
    }
    /**
     * Verifies that null instrumentl values cannot be persisted.
     *
     * @throws Exception if an unexpected error occurs
     */
    @Test
    public void nullSecurityTypeFail()
            throws Exception
    {
        final ExecutionReport report = removeField(createDummyExecReport(),
                                                   Symbol.FIELD);
        assertNull(report.getInstrument());
        nonNullCVCheck("securityType",
                       new Callable<Object>() {
            public Object call()
                    throws Exception
            {
                reportHistoryServices.save(report);
                return null;
            }
        });
    }
    /**
     * 
     *
     *
     * @return an <code>I</code> value
     */
    protected abstract I getInstrument();
    /**
     * 
     *
     *
     * @param inSummary
     * @param inAvgPrice
     * @param inCumQuantity
     * @param inLastPrice
     * @param inLastQuantity
     * @param inOrderID
     * @param inOrderStatus
     * @param inOrigOrderID
     * @param inReport
     * @param inRootID
     * @param inSendingTime
     * @param inSide
     * @param inInstrument
     * @throws Exception
     */
    protected void assertSummary(ExecutionReportSummary inSummary,
                                 BigDecimal inAvgPrice,
                                 BigDecimal inCumQuantity,
                                 BigDecimal inLastPrice,
                                 BigDecimal inLastQuantity,
                                 OrderID inOrderID,
                                 OrderStatus inOrderStatus,
                                 OrderID inOrigOrderID,
                                 PersistentReport inReport,
                                 OrderID inRootID,
                                 Date inSendingTime,
                                 Side inSide,
                                 I inInstrument)
            throws Exception
    {
        assertBigDecimalEquals(inAvgPrice,
                               inSummary.getAvgPrice());
        assertBigDecimalEquals(inCumQuantity,
                               inSummary.getCumQuantity());
        assertBigDecimalEquals(inLastPrice,
                               inSummary.getLastPrice());
        assertBigDecimalEquals(inLastQuantity,
                               inSummary.getLastQuantity());
        assertEquals(inOrderID,
                     inSummary.getOrderID());
        assertEquals(inOrderStatus,
                     inSummary.getOrderStatus());
        assertEquals(inOrigOrderID,
                     inSummary.getOrigOrderID());
        assertReportEquals(inReport.toReport(),
                           inSummary.getReport().toReport());
        assertEquals(inReport.getViewerID(),
                     inSummary.getViewerID());
        assertEquals(inRootID,
                     inSummary.getRootID());
        assertCalendarEquals(inSendingTime,
                             inSummary.getSendingTime(),
                             TemporalType.TIMESTAMP);
        assertEquals(inSide,
                     inSummary.getSide());
        assertInstrument(inSummary,
                         inInstrument);
    }

    protected void assertInstrument(ExecutionReportSummary inSummary, I inInstrument) {
        assertEquals(inInstrument.getSecurityType(), inSummary.getSecurityType());
        assertEquals(inInstrument.getSymbol(), inSummary.getSymbol());
    }

    private static ExecutionReport removeField(ExecutionReport inReport,
                                               int inTag) {
        ((HasFIXMessage)inReport).getMessage().removeField(inTag);
        return inReport;
    }
    /**
     * 
     *
     *
     * @return
     * @throws Exception
     */
    protected ExecutionReport createDummyExecReport()
            throws Exception
    {
        return createExecReport("o1",
                                null,
                                getInstrument(),
                                Side.Buy,
                                OrderStatus.Filled,
                                BigDecimal.ONE,
                                BigDecimal.ONE,
                                BigDecimal.ONE,
                                BigDecimal.ONE);
    }
}
