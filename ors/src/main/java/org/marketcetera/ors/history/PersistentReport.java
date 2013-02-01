package org.marketcetera.ors.history;

import java.util.Date;

import javax.persistence.*;

import org.marketcetera.ors.Principals;
import org.marketcetera.ors.security.User;
import org.marketcetera.persist.EntityBase;
import org.marketcetera.trade.*;
import org.marketcetera.util.log.I18NBoundMessage1P;
import org.marketcetera.util.misc.ClassVersion;

import quickfix.InvalidMessage;
import quickfix.Message;

/* $License$ */
/**
 * A persistent report. The report instance is persisted to maintain
 * history. The reports can be retrieved filtered / sorted by the timestamp
 * of when they were sent.
 *
 * @author anshul@marketcetera.com
 * @version $Id$
 * @since 1.0.0
 */
@Entity
@Table(name="reports")
//@NamedQuery(name = "forOrderID",query = "select e from PersistentReport e where e.orderID = :orderID")
@ClassVersion("$Id$")
class PersistentReport
        extends EntityBase
{
    /**
     * Returns the principals associated with the report with given
     * order ID.
     *
     * @param orderID The order ID.
     *
     * @return The principals. If no report with the given order ID
     * exists, {@link Principals#UNKNOWN} is returned, and no
     * exception is thrown.
     *
     * @throws PersistenceException if there were errors accessing the
     * report.
     */
    static Principals getPrincipals(final OrderID orderID)
    {
//        return executeRemote(new Transaction<Principals>() {
//            private static final long serialVersionUID=1L;
//
//            @Override
//            public Principals execute
//                (EntityManager em,
//                 PersistContext context)
//            {
//                Query query=em.createNamedQuery("forOrderID"); //$NON-NLS-1$
//                query.setParameter("orderID",orderID); //$NON-NLS-1$
//                List<?> list=query.getResultList();
//                if (list.isEmpty()) {
//                    return Principals.UNKNOWN;
//                }
//                PersistentReport report=(PersistentReport)(list.get(0));
//                return new Principals(report.getActorID(),
//                                      report.getViewerID());
//            }
//        },null);
        throw new UnsupportedOperationException(); // TODO COLIN
    }
    /**
     * Creates an instance, given a report.
     *
     * @param inReport the report instance.
     *
     * @throws PersistenceException if there were errors creating the
     * instance.
     */
    PersistentReport(ReportBase inReport)
    {
//        mReportBase = inReport;
//        setBrokerID(inReport.getBrokerID());
//        setSendingTime(inReport.getSendingTime());
//        if(inReport instanceof HasFIXMessage) {
//            setFixMessage(((HasFIXMessage) inReport).getMessage().toString());
//        }
//        setOriginator(inReport.getOriginator());
//        setOrderID(inReport.getOrderID());
//        setReportID(inReport.getReportID());
//        if(inReport.getActorID()!=null) {
//            setActor(new SingleSimpleUserQuery(inReport.getActorID().getValue()).fetch());
//        }
//        if (inReport.getViewerID()!=null) {
//            setViewer(new SingleSimpleUserQuery
//                      (inReport.getViewerID().getValue()).fetch());
//        }
//        if(inReport instanceof ExecutionReport) {
//            mReportType = ReportType.ExecutionReport;
//        } else if (inReport instanceof OrderCancelReject) {
//            mReportType = ReportType.CancelReject;
//        } else {
//            //You added new report types but forgot to update the code
//            //to persist them.
//            throw new IllegalArgumentException();
//        }
        throw new UnsupportedOperationException(); // TODO COLIN
    }


    /**
     * Converts the report into a system report instance.
     *
     * @return the system report instance.
     *
     * @throws ReportPersistenceException if there were errors converting
     * the message from its persistent representation to system report
     * instance.
     */
    ReportBase toReport() throws ReportPersistenceException {
        ReportBase returnValue = null;
        String fixMsgString = null;
        try {
            fixMsgString = getFixMessage();
            Message fixMessage = new Message(fixMsgString);
            switch(mReportType) {
                case ExecutionReport:
                    returnValue =  Factory.getInstance().createExecutionReport(
                            fixMessage, getBrokerID(),
                            getOriginator(), getActorID(), getViewerID());
                    break;
                case CancelReject:
                    returnValue =  Factory.getInstance().createOrderCancelReject(
                            fixMessage, getBrokerID(), getOriginator(), getActorID(), getViewerID());
                    break;
                default:
                    //You added new report types but forgot to update the code
                    //to persist them.
                    throw new IllegalArgumentException();
            }
            ReportBaseImpl.assignReportID((ReportBaseImpl)returnValue,
                                          getReportID());
            return returnValue;
        } catch (InvalidMessage e) {
            throw new ReportPersistenceException(e, new I18NBoundMessage1P(
                    Messages.ERROR_RECONSTITUTE_FIX_MSG, fixMsgString));
        } catch (MessageCreationException e) {
            throw new ReportPersistenceException(e, new I18NBoundMessage1P(
                    Messages.ERROR_RECONSTITUTE_FIX_MSG, fixMsgString));
        }
    }
//    @Override
//    protected void postSaveLocal(EntityManager em,
//                                 EntityBase merged,
//                                 PersistContext context)
//    {
//        super.postSaveLocal(em, merged, context);
//        PersistentReport mergedReport = (PersistentReport) merged;
//        //Save the summary if the report is an execution report.
//        if(mergedReport.getReportType() == ReportType.ExecutionReport) {
//            new ExecutionReportSummary((ExecutionReport)mReportBase,
//                                       mergedReport).localSave(em,
//                                                               context);
//        }
//    }

    private Originator getOriginator() {
        return mOriginator;
    }

    private void setOriginator(Originator inOriginator) {
        mOriginator = inOriginator;
    }

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="value",
                    column = @Column(name = "orderID", nullable = false))})
    OrderID getOrderID() {
        return mOrderID;
    }

    private void setOrderID(OrderID inOrderID) {
        mOrderID = inOrderID;
    }

    @ManyToOne
    public User getActor() {
        return mActor;
    }

    private void setActor(User inActor) {
        mActor = inActor;
    }

    @Transient
    UserID getActorID() {
        if (getActor()==null) {
            return null;
        }
        return getActor().getUserID();
    }

    @ManyToOne
    public User getViewer() {
        return mViewer;
    }

    private void setViewer(User inViewer) {
        mViewer = inViewer;
    }

    @Transient
    UserID getViewerID() {
        if (getViewer()==null) {
            return null;
        }
        return getViewer().getUserID();
    }

    @Transient
    BrokerID getBrokerID() {
        return mBrokerID;
    }

    private void setBrokerID(BrokerID inBrokerID) {
        mBrokerID = inBrokerID;
    }
    @Column(name = "brokerID")
    @SuppressWarnings("unused")
    private String getBrokerIDAsString() {
        return getBrokerID() == null
                ? null
                : getBrokerID().toString();
    }
    @SuppressWarnings("unused")
    private void setBrokerIDAsString(String inValue) {
        setBrokerID(inValue == null
                ? null
                : new BrokerID(inValue));
    }

    @Transient
    ReportID getReportID() {
        return mReportID;
    }

    private void setReportID(ReportID inReportID) {
        mReportID = inReportID;
    }
    @Column(name = "reportID", nullable = false)
    @SuppressWarnings("unused")
    private long getReportIDAsLong() {
        return getReportID().longValue();
    }
    @SuppressWarnings("unused")
    private void setReportIDAsLong(long inValue) {
        setReportID(new ReportID(inValue));
    }

    @Lob
    @Column(nullable = false)
    private String getFixMessage() {
        return mFixMessage;
    }

    private void setFixMessage(String inFIXMessage) {
        mFixMessage = inFIXMessage;
    }

    @Column(nullable = false)
    @SuppressWarnings("unused")
    private Date getSendingTime() {
        return mSendingTime;
    }

    private void setSendingTime(Date inSendingTime) {
        mSendingTime = inSendingTime;
    }

    @Column(nullable = false)
    private ReportType getReportType() {
        return mReportType;
    }

    @SuppressWarnings("unused")
    private void setReportType(ReportType inReportType) {
        mReportType = inReportType;
    }

    /**
     * Declared to get JPA to work.
     */
    PersistentReport() {
    }

    /**
     * The attribute sending time used in JPQL queries
     */
    static final String ATTRIBUTE_SENDING_TIME = "sendingTime";  //$NON-NLS-1$
    /**
     * The attribute actor used in JPQL queries
     */
    static final String ATTRIBUTE_ACTOR = "actor";  //$NON-NLS-1$
    /**
     * The attribute viewer used in JPQL queries
     */
    static final String ATTRIBUTE_VIEWER = "viewer";  //$NON-NLS-1$
    /**
     * The entity name as is used in various JPQL Queries
     */
    static final String ENTITY_NAME = PersistentReport.class.getSimpleName();

    private Originator mOriginator;
    private OrderID mOrderID;
    private User mActor; 
    private User mViewer; 
    private BrokerID mBrokerID;
    private ReportID mReportID;
    private String mFixMessage;
    private Date mSendingTime;
    private ReportType mReportType;
    private ReportBase mReportBase;
    private static final long serialVersionUID = 1;
}
