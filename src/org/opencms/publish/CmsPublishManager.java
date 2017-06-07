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

import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This manager provide access to the publish engine runtime information.<p>
 *
 * @since 6.5.5
 */
public class CmsPublishManager {

    /**
     * Enum for the different publish  list remove modes.<p>
     */
    public static enum PublishListRemoveMode {
        /** Remove publish list entries for all users if a resource is published. */
        allUsers, /** Remove publish list entry for the current user if a resource is published. */
        currentUser
    }

    /** The default history size. */
    public static final int DEFAULT_HISTORY_SIZE = 100;

    /** The default persistence setting for the publish queue. */
    public static final boolean DEFAULT_QUEUE_PERSISTANCE = false;

    /** The default shutdown time for the running publish job. */
    public static final int DEFAULT_QUEUE_SHUTDOWNTIME = 1;

    /** Milliseconds in a second. */
    private static final int MS_ONE_SECOND = 1000;

    /** Indicates if the configuration can be modified. */
    private boolean m_frozen;

    /** The underlying publish engine. */
    private CmsPublishEngine m_publishEngine;

    /** The maximum size of the publish history. */
    private int m_publishHistorySize;

    /** Publish job verifier. */
    private CmsPublishListVerifier m_publishListVerifier = new CmsPublishListVerifier();

    /** The publish list remove mode. */
    private CmsPublishManager.PublishListRemoveMode m_publishListRemoveMode;

    /** Indicates if the publish queue is re-initialized on startup. */
    private boolean m_publishQueuePersistance;

    /** The amount of time to wait for a publish job during shutdown. */
    private int m_publishQueueShutdowntime;

    /** The security manager. */
    private CmsSecurityManager m_securityManager;

    /**
     * Default constructor used in digester initialization.<p>
     */
    public CmsPublishManager() {

        m_publishEngine = null;
        m_frozen = false;
    }

    /**
     * Constructor used to create a pre-initialized instance.<p>
     *
     * @param historySize the size of the publish history
     * @param queuePersistance indicates if the queue is re-initialized on startup
     * @param queueShutdowntime the amount of time to wait for a publish job during shutdown
     */
    public CmsPublishManager(int historySize, boolean queuePersistance, int queueShutdowntime) {

        m_publishEngine = null;
        m_publishHistorySize = historySize;
        m_publishQueuePersistance = queuePersistance;
        m_publishQueueShutdowntime = queueShutdowntime;
        m_frozen = false;
    }

    /**
     * Abandons the current publish thread.<p>
     */
    public void abandonThread() {

        m_publishEngine.abandonThread();
    }

    /**
     * Aborts the given publish job.<p>
     *
     * @param cms the cms context
     * @param publishJob the publish job to abort
     * @param removeJob indicates if the job will be removed or added to history
     *
     * @throws CmsException if there is some problem during unlocking the resources
     * @throws CmsSecurityException if the current user has not enough permissions
     * @throws CmsPublishException if the publish job can not been aborted
     */
    public void abortPublishJob(CmsObject cms, CmsPublishJobEnqueued publishJob, boolean removeJob)
    throws CmsException, CmsSecurityException, CmsPublishException {

        if (!OpenCms.getRoleManager().hasRole(cms, CmsRole.PROJECT_MANAGER)
            && !cms.getRequestContext().getCurrentUser().getId().equals(publishJob.getUserId())) {
            // Can only be executed by somebody with the role CmsRole#PROJECT_MANAGER or the owner of the job
            throw new CmsSecurityException(
                Messages.get().container(
                    Messages.ERR_PUBLISH_ENGINE_ABORT_DENIED_1,
                    cms.getRequestContext().getCurrentUser().getName()));
        }
        m_publishEngine.abortPublishJob(cms.getRequestContext().getCurrentUser().getId(), publishJob, removeJob);
    }

    /**
     * Adds a publish listener to listen on publish events.<p>
     *
     * @param listener the publish listener to add
     */
    public void addPublishListener(I_CmsPublishEventListener listener) {

        m_publishEngine.addPublishListener(listener);
    }

    /**
     * Check if the thread for the current publish job is still active or was interrupted
     * and so the next job in the queue can be started.<p>
     */
    public void checkCurrentPublishJobThread() {

        m_publishEngine.checkCurrentPublishJobThread();
    }

    /**
     * Disables the publishing of resources.<p>
     */
    public void disablePublishing() {

        m_publishEngine.disableEngine();
    }

    /**
     * Enables the enqeueing of resources for publishing.<p>
     */
    public void enablePublishing() {

        m_publishEngine.enableEngine();
    }

    /**
     * Returns the current running publish job.<p>
     *
     * @return the current running publish job
     */
    public CmsPublishJobRunning getCurrentPublishJob() {

        if (m_publishEngine.getCurrentPublishJob() == null) {
            return null;
        }
        return new CmsPublishJobRunning(m_publishEngine.getCurrentPublishJob().getPublishJob());
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

        return m_publishEngine.getJobByPublishHistoryId(publishHistoryId);
    }

    /**
     * Returns the publish history list with already publish jobs.<p>
     *
     * @return a list of {@link CmsPublishJobFinished} objects
     */
    public List<CmsPublishJobFinished> getPublishHistory() {

        return m_publishEngine.getPublishHistory().asList();
    }

    /**
     * Returns the publish history list with already publish jobs, filtered by the given user.<p>
     *
     * @param user the user to filter the jobs with
     *
     * @return a list of {@link CmsPublishJobFinished} objects
     */
    public List<CmsPublishJobFinished> getPublishHistory(CmsUser user) {

        List<CmsPublishJobFinished> result = new ArrayList<CmsPublishJobFinished>();
        Iterator<CmsPublishJobFinished> it = getPublishHistory().iterator();
        while (it.hasNext()) {
            CmsPublishJobFinished publishJob = it.next();
            if (publishJob.getUserId().equals(user.getId())) {
                result.add(publishJob);
            }
        }
        return result;
    }

    /**
     * Returns the publish History Size.<p>
     *
     * @return the publish History Size
     */
    public int getPublishHistorySize() {

        return m_publishHistorySize;
    }

    /**
     * Gets the publish job verifier.<p>
     *
     * @return the publish job verifier
     */
    public CmsPublishListVerifier getPublishListVerifier() {

        return m_publishListVerifier;
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published.<p>
     *
     * @param cms the cms request context
     *
     * @return a publish list
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList getPublishList(CmsObject cms) throws CmsException {

        return m_securityManager.fillPublishList(
            cms.getRequestContext(),
            new CmsPublishList(cms.getRequestContext().getCurrentProject()));
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published for a direct publish of a single resource.<p>
     *
     * @param cms the cms request context
     * @param directPublishResource the resource which will be directly published
     * @param directPublishSiblings <code>true</code>, if all eventual siblings of the direct
     *                      published resource should also get published.
     *
     * @return a publish list
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList getPublishList(
        CmsObject cms,
        CmsResource directPublishResource,
        boolean directPublishSiblings) throws CmsException {

        return m_securityManager.fillPublishList(
            cms.getRequestContext(),
            new CmsPublishList(directPublishResource, directPublishSiblings));
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published for a direct publish of a List of resources.<p>
     *
     * @param cms the cms request context
     * @param directPublishResources the resources which will be directly published
     * @param directPublishSiblings <code>true</code>, if all eventual siblings of the direct
     *                      published resources should also get published.
     *
     * @return a publish list
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList getPublishList(
        CmsObject cms,
        List<CmsResource> directPublishResources,
        boolean directPublishSiblings) throws CmsException {

        return getPublishList(cms, directPublishResources, directPublishSiblings, true);
    }

    /**
     * Returns a publish list with all new/changed/deleted resources of the current (offline)
     * project that actually get published for a direct publish of a List of resources.<p>
     *
     * @param cms the cms request context
     * @param directPublishResources the {@link CmsResource} objects which will be directly published
     * @param directPublishSiblings <code>true</code>, if all eventual siblings of the direct
     *                      published resources should also get published.
     * @param publishSubResources indicates if sub-resources in folders should be published (for direct publish only)
     *
     * @return a publish list
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList getPublishList(
        CmsObject cms,
        List<CmsResource> directPublishResources,
        boolean directPublishSiblings,
        boolean publishSubResources) throws CmsException {

        return m_securityManager.fillPublishList(
            cms.getRequestContext(),
            new CmsPublishList(directPublishResources, directPublishSiblings, publishSubResources));
    }

    /**
     * Returns a publish list with all the given resources, filtered only by state.<p>
     *
     * @param cms the cms request context
     * @param directPublishResources the {@link CmsResource} objects which will be directly published
     * @param directPublishSiblings <code>true</code>, if all eventual siblings of the direct
     *                      published resources should also get published
     * @param isUserPublishList if true, the publish list consists of resources directly selected by the user to publish
     *
     * @return a publish list
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList getPublishListAll(
        CmsObject cms,
        List<CmsResource> directPublishResources,
        boolean directPublishSiblings,
        boolean isUserPublishList) throws CmsException {

        CmsPublishList pubList = new CmsPublishList(true, directPublishResources, directPublishSiblings);
        pubList.setUserPublishList(isUserPublishList);

        return m_securityManager.fillPublishList(cms.getRequestContext(), pubList);
    }

    /**
     * Gets the publish list remove mode.<p>
     *
     * @return the publish list remove mode
     */
    public CmsPublishManager.PublishListRemoveMode getPublishListRemoveMode() {

        return m_publishListRemoveMode;
    }

    /**
     * Returns the queue with still waiting publish jobs.<p>
     *
     * @return a list of {@link CmsPublishJobEnqueued} objects
     */
    public List<CmsPublishJobEnqueued> getPublishQueue() {

        return m_publishEngine.getPublishQueue().asList();
    }

    /**
     * Returns the amount of time in seconds the system will wait during shutdown for a running publish job.<p>
     *
     * @return the shutdown time for a running publish job
     */
    public int getPublishQueueShutdowntime() {

        return m_publishQueueShutdowntime;
    }

    /**
     * Returns a new publish list that contains the unpublished resources related
     * to all resources in the given publish list, the related resources exclude
     * all resources in the given publish list and also locked (by other users) resources.<p>
     *
     * @param cms the cms request context
     * @param publishList the publish list to exclude from result
     *
     * @return a new publish list that contains the related resources
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList getRelatedResourcesToPublish(CmsObject cms, CmsPublishList publishList) throws CmsException {

        return m_securityManager.getRelatedResourcesToPublish(
            cms.getRequestContext(),
            publishList,
            CmsRelationFilter.TARGETS.filterStrong());
    }

    /**
     * Returns the content of the publish report assigned to the given publish job.<p>
     *
     * @param publishJob the published job
     * @return the content of the assigned publish report
     *
     * @throws CmsException if something goes wrong
     */
    public byte[] getReportContents(CmsPublishJobFinished publishJob) throws CmsException {

        return m_publishEngine.getReportContents(publishJob);
    }

    /**
     * Returns the current user's publish list.<p>
     *
     * @param cms the current cms context
     *
     * @return the current user's publish list
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getUsersPubList(CmsObject cms) throws CmsException {

        return m_securityManager.getUsersPubList(cms.getRequestContext());
    }

    /**
     * Initializes the publish manager and the publish engine finally.<p>
     *
     * @param cms an admin cms object
     *
     * @throws CmsException if something goes wrong
     */
    public void initialize(CmsObject cms) throws CmsException {

        m_publishEngine.initialize(cms, m_publishQueuePersistance, m_publishQueueShutdowntime);
        m_frozen = true;
    }

    /**
     * Returns if the publish queue is persisted an will be re-initialized on startup.<p>
     *
     * @return <code>true</code> if the publish queue is persisted
     */
    public boolean isPublishQueuePersistanceEnabled() {

        return m_publishQueuePersistance;
    }

    /**
     * Returns the working state, that is if no publish job
     * is waiting to be processed and there is no current running
     * publish job.<p>
     *
     * @return the working state
     */
    public boolean isRunning() {

        return m_publishEngine.isRunning();
    }

    /**
     * Returns a new publish list that contains all resources of both given publish lists.<p>
     *
     * @param cms the cms request context
     * @param pubList1 the first publish list
     * @param pubList2 the second publish list
     *
     * @return a new publish list that contains all resources of both given publish lists
     *
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList mergePublishLists(CmsObject cms, CmsPublishList pubList1, CmsPublishList pubList2)
    throws CmsException {

        return m_securityManager.mergePublishLists(cms.getRequestContext(), pubList1, pubList2);
    }

    /**
     * Publishes the current project, printing messages to a shell report.<p>
     *
     * @param cms the cms request context
     * @return the publish history id of the published project
     *
     * @throws Exception if something goes wrong
     *
     * @see CmsShellReport
     */
    public CmsUUID publishProject(CmsObject cms) throws Exception {

        return publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
    }

    /**
     * Publishes the current project.<p>
     *
     * @param cms the cms request context
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     *
     * @return the publish history id of the published project
     *
     * @throws CmsException if something goes wrong
     */
    public CmsUUID publishProject(CmsObject cms, I_CmsReport report) throws CmsException {

        return publishProject(cms, report, getPublishList(cms));
    }

    /**
     * Publishes the resources of a specified publish list.<p>
     *
     * @param cms the cms request context
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * @param publishList a publish list
     *
     * @return the publish history id of the published project
     *
     * @throws CmsException if something goes wrong
     *
     * @see #getPublishList(CmsObject)
     * @see #getPublishList(CmsObject, CmsResource, boolean)
     * @see #getPublishList(CmsObject, List, boolean)
     */
    public CmsUUID publishProject(CmsObject cms, I_CmsReport report, CmsPublishList publishList) throws CmsException {

        return m_securityManager.publishProject(cms, publishList, report);
    }

    /**
     * Direct publishes a specified resource.<p>
     *
     * @param cms the cms request context
     * @param report an instance of <code>{@link I_CmsReport}</code> to print messages
     * @param directPublishResource a <code>{@link CmsResource}</code> that gets directly published;
     *                          or <code>null</code> if an entire project gets published.
     * @param directPublishSiblings if a <code>{@link CmsResource}</code> that should get published directly is
     *                          provided as an argument, all eventual siblings of this resource
     *                          get publish too, if this flag is <code>true</code>.
     *
     * @return the publish history id of the published project
     *
     * @throws CmsException if something goes wrong
     */
    public CmsUUID publishProject(
        CmsObject cms,
        I_CmsReport report,
        CmsResource directPublishResource,
        boolean directPublishSiblings) throws CmsException {

        return publishProject(cms, report, getPublishList(cms, directPublishResource, directPublishSiblings));
    }

    /**
     * Publishes a single resource, printing messages to a shell report.<p>
     *
     * The siblings of the resource will not be published.<p>
     *
     * @param cms the cms request context
     * @param resourcename the name of the resource to be published
     *
     * @return the publish history id of the published project
     *
     * @throws Exception if something goes wrong
     *
     * @see CmsShellReport
     */
    public CmsUUID publishResource(CmsObject cms, String resourcename) throws Exception {

        return publishResource(cms, resourcename, false, new CmsShellReport(cms.getRequestContext().getLocale()));
    }

    /**
     * Publishes a single resource.<p>
     *
     * @param cms the cms request context
     * @param resourcename the name of the resource to be published
     * @param publishSiblings if <code>true</code>, all siblings of the resource are also published
     * @param report the report to write the progress information to
     *
     * @return the publish history id of the published project
     *
     * @throws Exception if something goes wrong
     */
    public CmsUUID publishResource(CmsObject cms, String resourcename, boolean publishSiblings, I_CmsReport report)
    throws Exception {

        CmsResource resource = cms.readResource(resourcename, CmsResourceFilter.ALL);
        return publishProject(cms, report, resource, publishSiblings);
    }

    /**
     * Removes the given publish listener.<p>
     *
     * @param listener the publish listener to remove
     */
    public void removePublishListener(I_CmsPublishEventListener listener) {

        m_publishEngine.removePublishListener(listener);
    }

    /**
     * Removes the given resource to the given user's publish list.<p>
     *
     * @param cms the current cms context
     * @param structureIds the collection of structure IDs to remove
     *
     * @throws CmsException if something goes wrong
     */
    public void removeResourceFromUsersPubList(CmsObject cms, Collection<CmsUUID> structureIds) throws CmsException {

        m_securityManager.removeResourceFromUsersPubList(cms.getRequestContext(), structureIds);
    }

    /**
     * Sets the publish engine during initialization.<p>
     *
     * @param publishEngine the publish engine instance
     */
    public void setPublishEngine(CmsPublishEngine publishEngine) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        m_publishEngine = publishEngine;
    }

    /**
     * Sets the publish History Size.<p>
     *
     * @param publishHistorySize the publish History Size to set
     */
    public void setPublishHistorySize(String publishHistorySize) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        m_publishHistorySize = Integer.parseInt(publishHistorySize);
    }

    /**
     * Sets the publish list remove mode.<p>
     *
     * @param publishListRemoveMode the publish list remove mode
     */
    public void setPublishListRemoveMode(CmsPublishManager.PublishListRemoveMode publishListRemoveMode) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        m_publishListRemoveMode = publishListRemoveMode;
    }

    /**
     * Sets if the publish queue is re-initialized on startup.<p>
     *
     * @param publishQueuePersistance the persistence flag, parsed as <code>boolean</code>
     */
    public void setPublishQueuePersistance(String publishQueuePersistance) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        m_publishQueuePersistance = Boolean.valueOf(publishQueuePersistance).booleanValue();
    }

    /**
     * Sets the publish queue shutdown time.
     *
     * @param publishQueueShutdowntime the shutdown time to set, parsed as <code>int</code>
     */
    public void setPublishQueueShutdowntime(String publishQueueShutdowntime) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        m_publishQueueShutdowntime = Integer.parseInt(publishQueueShutdowntime);
    }

    /**
     * Sets the security manager during initialization.<p>
     *
     * @param securityManager the security manager
     */
    public void setSecurityManager(CmsSecurityManager securityManager) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        m_securityManager = securityManager;
    }

    /**
     * Starts publishing of enqueued publish jobs.<p>
     */
    public void startPublishing() {

        m_publishEngine.startEngine();
    }

    /**
     * Stops the publishing of enqueued publish jobs.<p>
     */
    public void stopPublishing() {

        m_publishEngine.stopEngine();
    }

    /**
     * Validates the relations for the given resources.<p>
     *
     * @param cms the cms request context
     * @param publishList the publish list to validate against the online project
     * @param report a report to write the messages to
     *
     * @return a map with lists of invalid links
     *          (<code>{@link org.opencms.relations.CmsRelation}}</code> objects)
     *          keyed by root paths
     *
     * TODO: change return value to List of CmsRelation
     *
     * @throws Exception if something goes wrong
     */
    public Map<String, List<CmsRelation>> validateRelations(
        CmsObject cms,
        CmsPublishList publishList,
        I_CmsReport report) throws Exception {

        return m_securityManager.validateRelations(cms.getRequestContext(), publishList, report);
    }

    /**
     * Waits until no publish jobs remain.<p>
     */
    public void waitWhileRunning() {

        waitWhileRunning(Long.MAX_VALUE);
    }

    /**
     * Waits until no publish jobs remain or the given max milliseconds.<p>
     *
     * @param ms the max milliseconds to wait
     */
    public void waitWhileRunning(long ms) {

        int i = 0;
        // wait until it is done or time is over
        synchronized (this) {
            try {
                Thread.sleep(100); // wait a bit to give the publish engine the chance to actualize the state
            } catch (InterruptedException e) {
                // ignore
                e.printStackTrace();
            }
            while (isRunning() && ((MS_ONE_SECOND * i) <= ms)) {
                try {
                    Thread.sleep(MS_ONE_SECOND); // wait a second
                } catch (InterruptedException e) {
                    // ignore
                    e.printStackTrace();
                }
                i++;
            }
        }
    }

    /**
     * Returns the currently used publish engine.<p>
     *
     * @return the publish engine
     */
    protected CmsPublishEngine getEngine() {

        return m_publishEngine;
    }
}
