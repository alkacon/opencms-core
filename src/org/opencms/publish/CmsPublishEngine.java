/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.publish;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsUserSettings;
import org.opencms.db.I_CmsDbContextFactory;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAuthentificationException;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * This class is responsible for the publish process.<p>
 *
 * @since 6.5.5
 */
public final class CmsPublishEngine {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishEngine.class);

    /** The id of the admin user. */
    private CmsUUID m_adminUserId;

    /** The current running publish job. */
    private CmsPublishThread m_currentPublishThread;

    /** The runtime info factory used during publishing. */
    private final I_CmsDbContextFactory m_dbContextFactory;

    /** The driver manager instance. */
    private CmsDriverManager m_driverManager;

    /** The engine state. */
    private CmsPublishEngineState m_engineState;

    /** The publish listeners. */
    private final CmsPublishListenerCollection m_listeners;

    /** The publish history list with already published jobs. */
    private final CmsPublishHistory m_publishHistory;

    /** The queue with still waiting publish job. */
    private final CmsPublishQueue m_publishQueue;

    /** The amount of time the system will wait for a running publish job during shutdown. */
    private int m_publishQueueShutdowntime;

    /** Is set during shutdown. */
    private boolean m_shuttingDown;

    /**
     * Default constructor.<p>
     *
     * @param dbContextFactory the initialized OpenCms runtime info factory
     *
     * @throws CmsInitException if the configured path to store the publish reports is not accessible
     */
    public CmsPublishEngine(I_CmsDbContextFactory dbContextFactory)
    throws CmsInitException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
            // OpenCms is already initialized
            throw new CmsInitException(
                org.opencms.main.Messages.get().container(org.opencms.main.Messages.ERR_ALREADY_INITIALIZED_0));
        }
        if (dbContextFactory == null) {
            throw new CmsInitException(
                org.opencms.main.Messages.get().container(org.opencms.main.Messages.ERR_CRITICAL_NO_DB_CONTEXT_0));
        }
        // initialize the db context factory
        m_dbContextFactory = dbContextFactory;
        // initialize publish queue
        m_publishQueue = new CmsPublishQueue(this);
        // initialize publish history
        m_publishHistory = new CmsPublishHistory(this);
        // initialize event handling
        m_listeners = new CmsPublishListenerCollection(this);
        // set engine state to normal processing
        m_engineState = CmsPublishEngineState.ENGINE_STARTED;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_PUBLISH_ENGINE_READY_0));
        }
    }

    /**
     * Abandons the given publish thread.<p>
     */
    public void abandonThread() {

        if (!m_currentPublishThread.isAlive()) {
            // thread is dead
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_ENGINE_DEAD_JOB_0));
            }
        } else {
            // thread is not dead, and we suppose it hangs :(
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    Messages.get().getBundle().key(
                        Messages.LOG_THREADSTORE_PUBLISH_THREAD_INTERRUPT_2,
                        m_currentPublishThread.getName(),
                        m_currentPublishThread.getUUID()));
            }
            m_currentPublishThread.interrupt();
        }
        // just throw it away
        m_currentPublishThread = null;
        // and try again
        checkCurrentPublishJobThread();
    }

    /**
     * Controls the publish process.<p>
     */
    public synchronized void checkCurrentPublishJobThread() {

        // give the finishing publish thread enough time to clean up
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // ignore
        }

        // create a publish thread only if engine is started
        if (m_engineState != CmsPublishEngineState.ENGINE_STARTED) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_ENGINE_RUNNING_0));
        }

        // check the driver manager
        if ((m_driverManager == null) || (m_dbContextFactory == null)) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_PUBLISH_ENGINE_NOT_INITIALIZED_0));
            // without these there is nothing we can do
            return;
        }

        // there is no running publish job
        if (m_currentPublishThread == null) {
            // but something is waiting in the queue
            if (!m_publishQueue.isEmpty()) {
                // start the next waiting publish job
                CmsPublishJobInfoBean publishJob = m_publishQueue.next();
                m_currentPublishThread = new CmsPublishThread(this, publishJob);
                m_currentPublishThread.start();
            } else {
                // nothing to do
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_ENGINE_NO_RUNNING_JOB_0));
                }
            }
            return;
        }
        if (m_currentPublishThread.isAlive()) {
            // normal running
            // wait until it is finished
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_ENGINE_WAITING_0));
            }
        } else {
            // clean up the dead thread
            abandonThread();
        }
    }

    /**
     * Enqueues a new publish job with the given information in publish queue.<p>
     *
     * All resources should already be locked.<p>
     *
     * If possible, the publish job starts immediately.<p>
     *
     * @param cms the cms context to publish for
     * @param publishList the resources to publish
     * @param report the report to write to
     *
     * @throws CmsException if something goes wrong while cloning the cms context
     */
    public void enqueuePublishJob(CmsObject cms, CmsPublishList publishList, I_CmsReport report) throws CmsException {

        // check the driver manager
        if ((m_driverManager == null) || (m_dbContextFactory == null)) {
            // the resources are unlocked in the driver manager
            throw new CmsPublishException(Messages.get().container(Messages.ERR_PUBLISH_ENGINE_NOT_INITIALIZED_0));
        }
        // prevent new jobs if the engine is disabled
        if (m_shuttingDown || (!isEnabled() && !OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN))) {
            // the resources are unlocked in the driver manager
            throw new CmsPublishException(Messages.get().container(Messages.ERR_PUBLISH_ENGINE_DISABLED_0));
        }

        // create the publish job
        CmsPublishJobInfoBean publishJob = new CmsPublishJobInfoBean(cms, publishList, report);
        try {
            // enqueue it and
            m_publishQueue.add(publishJob);
            // notify all listeners
            m_listeners.fireEnqueued(new CmsPublishJobBase(publishJob));
        } catch (Throwable t) {
            // we really really need to catch everything here, or else the queue status is broken
            if (m_publishQueue.contains(publishJob)) {
                m_publishQueue.remove(publishJob);
            }
            // throw the exception again
            throw new CmsException(
                Messages.get().container(Messages.ERR_PUBLISH_ENGINE_QUEUE_1, publishJob.getPublishHistoryId()),
                t);
        }
        // try to start the publish job immediately
        checkCurrentPublishJobThread();
    }

    /**
     * Returns a publish job based on its publish history id.<p>
     *
     * The returned publish job may be an enqueued, running or finished publish job.<p>
     *
     * @param publishHistoryId the publish history id to search for
     *
     * @return the publish job with the given publish history id, or <code>null</code>
     */
    public CmsPublishJobBase getJobByPublishHistoryId(CmsUUID publishHistoryId) {

        // try current running job
        if ((m_currentPublishThread != null)
            && m_currentPublishThread.getPublishJob().getPublishHistoryId().equals(publishHistoryId)) {
            return new CmsPublishJobRunning(m_currentPublishThread.getPublishJob());
        }
        // try enqueued jobs
        Iterator<CmsPublishJobEnqueued> itEnqueuedJobs = getPublishQueue().asList().iterator();
        while (itEnqueuedJobs.hasNext()) {
            CmsPublishJobEnqueued enqueuedJob = itEnqueuedJobs.next();
            if (enqueuedJob.getPublishList().getPublishHistoryId().equals(publishHistoryId)) {
                return enqueuedJob;
            }
        }
        // try finished jobs
        Iterator<CmsPublishJobFinished> itFinishedJobs = getPublishHistory().asList().iterator();
        while (itFinishedJobs.hasNext()) {
            CmsPublishJobFinished finishedJob = itFinishedJobs.next();
            if (finishedJob.getPublishHistoryId().equals(publishHistoryId)) {
                return finishedJob;
            }
        }
        return null;
    }

    /**
     * Sets the driver manager instance.<p>
     *
     * @param driverManager the driver manager instance
     */
    public void setDriverManager(CmsDriverManager driverManager) {

        m_driverManager = driverManager;
        CmsDbContext dbc = m_dbContextFactory.getDbContext();
        try {
            m_adminUserId = m_driverManager.readUser(dbc, OpenCms.getDefaultUsers().getUserAdmin()).getId();
        } catch (CmsException e) {
            dbc.rollback();
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Shuts down all this static export manager.<p>
     *
     * NOTE: this method may or may NOT be called (i.e. kill -9 in the stop script), if a system is stopped.<p>
     *
     * This is required since there may still be a thread running when the system is being shut down.<p>
     */
    public synchronized void shutDown() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                org.opencms.main.Messages.get().getBundle().key(
                    org.opencms.main.Messages.INIT_SHUTDOWN_START_1,
                    this.getClass().getName()));
        }

        // prevent new publish jobs are accepted
        m_shuttingDown = true;

        // if a job is currently running,
        // wait the specified amount of time,
        // then write an abort message to the report
        if (m_currentPublishThread != null) {

            // if a shutdown time is defined, wait  if a publish process is running
            if (m_publishQueueShutdowntime > 0) {
                synchronized (this) {
                    try {
                        Thread.sleep(m_publishQueueShutdowntime * 1000);
                    } catch (InterruptedException exc) {
                        // ignore
                    }
                }
            }

            if (m_currentPublishThread != null) {
                CmsPublishJobInfoBean publishJob = m_currentPublishThread.getPublishJob();
                try {
                    abortPublishJob(m_adminUserId, new CmsPublishJobEnqueued(publishJob), false);
                } catch (CmsException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }

        // write the log
        CmsDbContext dbc = getDbContext(null);
        try {
            m_driverManager.updateLog(dbc);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } finally {
            dbc.clear();
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                org.opencms.staticexport.Messages.get().getBundle().key(
                    org.opencms.staticexport.Messages.INIT_SHUTDOWN_1,
                    this.getClass().getName()));
        }
    }

    /**
     * Aborts the given publish job.<p>
     *
     * @param userId the id of user that wants to abort the given publish job
     * @param publishJob the publish job to abort
     * @param removeJob indicates if the job will be removed or added to history
     *
     * @throws CmsException if there is some problem during unlocking the resources
     * @throws CmsPublishException if the publish job can not be aborted
     */
    protected void abortPublishJob(CmsUUID userId, CmsPublishJobEnqueued publishJob, boolean removeJob)
    throws CmsException, CmsPublishException {

        // abort event should be raised before the job is removed implicitly
        m_listeners.fireAbort(userId, publishJob);

        if ((m_currentPublishThread == null)
            || !publishJob.m_publishJob.equals(m_currentPublishThread.getPublishJob())) {
            // engine is currently publishing another job or is not publishing
            if (!m_publishQueue.abortPublishJob(publishJob.m_publishJob)) {
                // job not found
                throw new CmsPublishException(
                    Messages.get().container(Messages.ERR_PUBLISH_ENGINE_MISSING_PUBLISH_JOB_0));
            }
        } else if (!m_shuttingDown) {
            // engine is currently publishing the job to abort
            m_currentPublishThread.abort();
        } else if (m_shuttingDown && (m_currentPublishThread != null)) {
            // aborting the current job during shut down
            I_CmsReport report = m_currentPublishThread.getReport();
            report.println();
            report.println();
            report.println(
                Messages.get().container(Messages.RPT_PUBLISH_JOB_ABORT_SHUTDOWN_0),
                I_CmsReport.FORMAT_ERROR);
            report.println();
        }

        // unlock all resources
        if (publishJob.getPublishList() != null) {
            unlockPublishList(publishJob.m_publishJob);
        }
        // keep job if requested
        if (!removeJob) {
            // set finish info
            publishJob.m_publishJob.finish();
            getPublishHistory().add(publishJob.m_publishJob);
        } else {
            getPublishQueue().remove(publishJob.m_publishJob);
        }
    }

    /**
     * Adds a publish listener to listen on publish events.<p>
     *
     * @param listener the publish listener to add
     */
    protected void addPublishListener(I_CmsPublishEventListener listener) {

        m_listeners.add(listener);
    }

    /**
     * Disables the publish engine, i.e. publish jobs are not accepted.<p>
     */
    protected void disableEngine() {

        m_engineState = CmsPublishEngineState.ENGINE_DISABLED;
    }

    /**
     * Enables the publish engine, i.e. publish jobs are accepted.<p>
     */
    protected void enableEngine() {

        m_engineState = CmsPublishEngineState.ENGINE_STARTED;
        // start publish job if jobs waiting
        if ((m_currentPublishThread == null) && !m_publishQueue.isEmpty()) {
            checkCurrentPublishJobThread();
        }
    }

    /**
     * Returns the current running publish job.<p>
     *
     * @return the current running publish job
     */
    protected CmsPublishThread getCurrentPublishJob() {

        return m_currentPublishThread;
    }

    /**
     * Returns the a new db context object.<p>
     *
     * @param ctx optional request context, can be <code>null</code>
     *
     * @return the a new db context object
     */
    protected CmsDbContext getDbContext(CmsRequestContext ctx) {

        return m_dbContextFactory.getDbContext(ctx);
    }

    /**
     * Returns the driver manager instance.<p>
     *
     * @return the driver manager instance
     */
    protected CmsDriverManager getDriverManager() {

        return m_driverManager;
    }

    /**
     * Returns the publish history list with already publish job.<p>
     *
     * @return the publish history list with already publish job
     */
    protected CmsPublishHistory getPublishHistory() {

        return m_publishHistory;
    }

    /**
     * Returns the queue with still waiting publish job.<p>
     *
     * @return the queue with still waiting publish job
     */
    protected CmsPublishQueue getPublishQueue() {

        return m_publishQueue;
    }

    /**
     * Returns the content of the publish report assigned to the given publish job.<p>
     *
     * @param publishJob the published job
     * @return the content of the assigned publish report
     *
     * @throws CmsException if something goes wrong
     */
    protected byte[] getReportContents(CmsPublishJobFinished publishJob) throws CmsException {

        byte[] result = null;
        CmsDbContext dbc = m_dbContextFactory.getDbContext();
        try {
            result = m_driverManager.readPublishReportContents(dbc, publishJob.getPublishHistoryId());
        } catch (CmsException e) {
            dbc.rollback();
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        } finally {
            dbc.clear();
        }
        return result;
    }

    /**
     * Returns the user identified by the given id.<p>
     *
     * @param userId the id of the user to retrieve
     *
     * @return the user identified by the given id
     */
    protected CmsUser getUser(CmsUUID userId) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext();
        try {
            return m_driverManager.readUser(dbc, userId);
        } catch (CmsException e) {
            dbc.rollback();
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            dbc.clear();
        }
        return null;
    }

    /**
     * Initializes the publish engine.<p>
     *
     * @param adminCms the admin cms
     * @param publishQueuePersistance flag if the queue is persisted
     * @param publishQueueShutdowntime amount of time to wait for a publish job during shutdown
     *
     * @throws CmsException if something goes wrong
     */
    protected void initialize(CmsObject adminCms, boolean publishQueuePersistance, int publishQueueShutdowntime)
    throws CmsException {

        // check the driver manager
        if ((m_driverManager == null) || (m_dbContextFactory == null)) {
            throw new CmsPublishException(Messages.get().container(Messages.ERR_PUBLISH_ENGINE_NOT_INITIALIZED_0));
        }

        m_publishQueueShutdowntime = publishQueueShutdowntime;

        // initially the engine is stopped, must be restartet after full system initialization
        m_engineState = CmsPublishEngineState.ENGINE_STOPPED;
        // read the publish history from the repository
        m_publishHistory.initialize();
        // read the queue from the repository
        m_publishQueue.initialize(adminCms, publishQueuePersistance);
    }

    /**
     * Returns the working state, that is if no publish job
     * is waiting to be processed and there is no current running
     * publish job.<p>
     *
     * @return the working state
     */
    protected boolean isRunning() {

        return (((m_engineState == CmsPublishEngineState.ENGINE_STARTED) && !m_publishQueue.isEmpty())
            || (m_currentPublishThread != null));
    }

    /**
     * Sets publish locks of resources in a publish list.<p>
     *
     * @param publishJob the publish job
     * @throws CmsException if something goes wrong
     */
    protected void lockPublishList(CmsPublishJobInfoBean publishJob) throws CmsException {

        CmsPublishList publishList = publishJob.getPublishList();
        // lock them
        CmsDbContext dbc = getDbContext(publishJob.getCmsObject().getRequestContext());
        try {
            Iterator<CmsResource> itResources = publishList.getAllResources().iterator();
            while (itResources.hasNext()) {
                CmsResource resource = itResources.next();
                m_driverManager.lockResource(dbc, resource, CmsLockType.PUBLISH);
            }
        } catch (CmsException e) {
            dbc.rollback();
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        } finally {
            dbc.clear();
        }
    }

    /**
     * Signalizes that the publish thread finishes.<p>
     *
     * @param publishJob the finished publish job
     */
    protected void publishJobFinished(CmsPublishJobInfoBean publishJob) {

        // in order to avoid not removable publish locks, unlock all assigned resources again
        try {
            unlockPublishList(publishJob);
        } catch (Throwable t) {
            // log failure, most likely a database problem
            LOG.error(t.getLocalizedMessage(), t);
        }

        // trigger the old event mechanism
        CmsDbContext dbc = m_dbContextFactory.getDbContext(publishJob.getCmsObject().getRequestContext());
        try {
            // fire an event that a project has been published
            Map<String, Object> eventData = new HashMap<String, Object>();
            eventData.put(I_CmsEventListener.KEY_REPORT, publishJob.getPublishReport());
            eventData.put(
                I_CmsEventListener.KEY_PUBLISHID,
                publishJob.getPublishList().getPublishHistoryId().toString());
            eventData.put(I_CmsEventListener.KEY_PROJECTID, dbc.currentProject().getUuid());
            eventData.put(I_CmsEventListener.KEY_DBCONTEXT, dbc);
            CmsEvent afterPublishEvent = new CmsEvent(I_CmsEventListener.EVENT_PUBLISH_PROJECT, eventData);
            OpenCms.fireCmsEvent(afterPublishEvent);
        } catch (Throwable t) {
            if (dbc != null) {
                dbc.rollback();
            }
            LOG.error(t.getLocalizedMessage(), t);
            // catch every thing including runtime exceptions
            publishJob.getPublishReport().println(t);
        } finally {
            if (dbc != null) {
                try {
                    dbc.clear();
                } catch (Throwable t) {
                    // ignore
                }
                dbc = null;
            }
        }
        try {
            // fire the publish finish event
            m_listeners.fireFinish(new CmsPublishJobRunning(publishJob));
        } catch (Throwable t) {
            // log failure, most likely a database problem
            LOG.error(t.getLocalizedMessage(), t);
        }
        try {
            // finish the job
            publishJob.finish();
        } catch (Throwable t) {
            // log failure, most likely a database problem
            LOG.error(t.getLocalizedMessage(), t);
        }
        try {
            // put the publish job into the history list
            m_publishHistory.add(publishJob);
        } catch (Throwable t) {
            // log failure, most likely a database problem
            LOG.error(t.getLocalizedMessage(), t);
        }
        if (Thread.currentThread() == m_currentPublishThread) {
            // wipe the dead thread, only if this thread has not been abandoned
            m_currentPublishThread = null;
        }
        // clear the published resources cache
        OpenCms.getMemoryMonitor().flushCache(CmsMemoryMonitor.CacheType.PUBLISHED_RESOURCES);
        // try to start a new publish job
        checkCurrentPublishJobThread();
    }

    /**
     * A publish job has been permanently removed from the history.<p>
     *
     * @param publishJob the removed publish job
     */
    protected void publishJobRemoved(CmsPublishJobInfoBean publishJob) {

        // a publish job has been removed
        m_listeners.fireRemove(new CmsPublishJobFinished(publishJob));
    }

    /**
     * Signalizes that the publish thread starts.<p>
     *
     * @param publishJob the started publish job
     */
    protected void publishJobStarted(CmsPublishJobInfoBean publishJob) {

        // update the job
        m_publishQueue.update(publishJob);

        // fire the publish start event
        m_listeners.fireStart(new CmsPublishJobEnqueued(publishJob));
    }

    /**
     * Removes the given publish listener.<p>
     *
     * @param listener the publish listener to remove
     */
    protected void removePublishListener(I_CmsPublishEventListener listener) {

        m_listeners.remove(listener);
    }

    /**
     * Sends a message to the given user, if publish notification is enabled or an error is shown in the message.<p>
     *
     * @param toUserId the id of the user to send the message to
     * @param message the message to send
     * @param hasErrors flag to determine if the message to send shows an error
     */
    protected void sendMessage(CmsUUID toUserId, String message, boolean hasErrors) {

        CmsDbContext dbc = m_dbContextFactory.getDbContext();
        try {
            CmsUser toUser = m_driverManager.readUser(dbc, toUserId);
            CmsUserSettings settings = new CmsUserSettings(toUser);
            if (settings.getShowPublishNotification() || hasErrors) {
                // only show message if publish notification is enabled or the message shows an error
                OpenCms.getSessionManager().sendBroadcast(null, message, toUser);
            }
        } catch (CmsException e) {
            dbc.rollback();
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            dbc.clear();
        }
    }

    /**
     * Starts the publish engine, i.e. publish jobs are accepted and processed.<p>
     */
    protected void startEngine() {

        if (m_engineState != CmsPublishEngineState.ENGINE_STARTED) {
            m_engineState = CmsPublishEngineState.ENGINE_STARTED;
            // start publish job if jobs waiting
            if ((m_currentPublishThread == null) && !m_publishQueue.isEmpty()) {
                checkCurrentPublishJobThread();
            }
        }
    }

    /**
     * Stops the publish engine, i.e. publish jobs are still accepted but not published.<p>
     */
    protected void stopEngine() {

        m_engineState = CmsPublishEngineState.ENGINE_STOPPED;
    }

    /**
     * Removes all publish locks of resources in a publish list of a publish job.<p>
     *
     * @param publishJob the publish job
     * @throws CmsException if something goes wrong
     */
    protected void unlockPublishList(CmsPublishJobInfoBean publishJob) throws CmsException {

        CmsPublishList publishList = publishJob.getPublishList();
        List<CmsResource> allResources = publishList.getAllResources();
        // unlock them
        CmsDbContext dbc = getDbContext(publishJob.getCmsObject().getRequestContext());
        try {
            Iterator<CmsResource> itResources = allResources.iterator();
            while (itResources.hasNext()) {
                CmsResource resource = itResources.next();
                m_driverManager.unlockResource(dbc, resource, true, true);
            }
        } catch (CmsException e) {
            dbc.rollback();
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        } finally {
            dbc.clear();
        }
    }

    /**
     * Returns <code>true</code> if the login manager allows login.<p>
     *
     * @return if enabled
     */
    private boolean isEnabled() {

        try {
            if ((m_engineState == CmsPublishEngineState.ENGINE_STOPPED)
                || (m_engineState == CmsPublishEngineState.ENGINE_STARTED)) {
                OpenCms.getLoginManager().checkLoginAllowed();
                return true;
            } else {
                return false;
            }
        } catch (CmsAuthentificationException e) {
            return false;
        }
    }
}
