/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishManager.java,v $
 * Date   : $Date: 2007/03/26 09:45:55 $
 * Version: $Revision: 1.1.2.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This manager provide access to the publish engine runtime information.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.9 $
 * 
 * @since 6.5.5
 */
public class CmsPublishManager {

    /** Milliseconds in a second. */
    private static final int MS_ONE_SECOND = 1000;

    /** The underlying publish engine. */
    private final CmsPublishEngine m_publishEngine;

    /** The publish relation filter to use. */
    private CmsRelationFilter m_publishRelationFilter;

    /** The security manager. */
    private CmsSecurityManager m_securityManager;

    /**
     * Default constructor.<p>
     * 
     * @param engine the underlying publish engine
     * @param securityManager the security manager
     */
    public CmsPublishManager(CmsPublishEngine engine, CmsSecurityManager securityManager) {

        m_publishEngine = engine;
        m_securityManager = securityManager;
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
            && !cms.getRequestContext().currentUser().getName().equals(publishJob.getUserName())) {
            // Can only be executed by somebody with the role CmsRole#PROJECT_MANAGER or the owner of the job
            throw new CmsSecurityException(Messages.get().container(
                Messages.ERR_PUBLISH_ENGINE_ABORT_DENIED_1,
                cms.getRequestContext().currentUser().getName()));
        }
        m_publishEngine.abortPublishJob(cms.getRequestContext().currentUser().getName(), publishJob, removeJob);
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
     * @param publishHistoryId the publish hostory id to search for
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
    public List getPublishHistory() {

        return m_publishEngine.getPublishHistory().asList();
    }

    /**
     * Returns the publish history list with already publish jobs, filtered by the given user.<p>
     * 
     * @param user the user to filter the jobs with
     * 
     * @return a list of {@link CmsPublishJobFinished} objects
     */
    public List getPublishHistory(CmsUser user) {

        List result = new ArrayList();
        Iterator it = getPublishHistory().iterator();
        while (it.hasNext()) {
            CmsPublishJobFinished publishJob = (CmsPublishJobFinished)it.next();
            if (publishJob.getUserName().equals(user.getName())) {
                result.add(publishJob);
            }
        }
        return result;
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

        return m_securityManager.fillPublishList(cms.getRequestContext(), new CmsPublishList(
            cms.getRequestContext().currentProject()));
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
    public CmsPublishList getPublishList(CmsObject cms, CmsResource directPublishResource, boolean directPublishSiblings)
    throws CmsException {

        return m_securityManager.fillPublishList(cms.getRequestContext(), new CmsPublishList(
            directPublishResource,
            directPublishSiblings));
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
    public CmsPublishList getPublishList(CmsObject cms, List directPublishResources, boolean directPublishSiblings)
    throws CmsException {

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
        List directPublishResources,
        boolean directPublishSiblings,
        boolean publishSubResources) throws CmsException {

        return m_securityManager.fillPublishList(cms.getRequestContext(), new CmsPublishList(
            directPublishResources,
            directPublishSiblings,
            publishSubResources));
    }

    /**
     * Returns the queue with still waiting publish jobs.<p>
     * 
     * @return a list of {@link CmsPublishJobEnqueued} objects
     */
    public List getPublishQueue() {

        return m_publishEngine.getPublishQueue().asList();
    }

    /**
     * Initializes the publish manager and the publish engine finally.<p>
     * 
     * @param cms an admin cms object
     * 
     * @throws CmsException if something goes wrong
     */
    public void initialize(CmsObject cms)
    throws CmsException {
    
        m_publishEngine.initialize(cms);
    }
    
    /**
     * Returns a new publish list that contains the unpublished resources related to the given resources, 
     * (or to all resources in the given publish list if the resource is <code>null</code>), the related 
     * resources exclude all resources in the given publish list.<p>
     * 
     * @param cms the cms request context
     * @param publishList the publish list to exclude from result or 
     *          get the related resources for if the resource is <code>null</code>
     * @param resource the resource to get the related resources for or 
     *          <code>null</code> to use all resources in the given publish list
     * 
     * @return a new publish list that contains the related resources
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsPublishList getRelatedResourcesToPublish(CmsObject cms, CmsPublishList publishList, CmsResource resource) throws CmsException {

        return m_securityManager.getRelatedResourcesToPublish(
            cms.getRequestContext(),
            publishList,
            resource,
            getPublishRelationFilter());
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
            while (isRunning() && ((MS_ONE_SECOND * i) <= ms)) {
                try {
                    this.wait(MS_ONE_SECOND); // wait a sec
                    i++;
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Returns the publish relation filter to use.<p>
     * 
     * @return the publish relation filter to use
     */
    private CmsRelationFilter getPublishRelationFilter() {

        if (m_publishRelationFilter == null) {
            m_publishRelationFilter = CmsRelationFilter.TARGETS;
            m_publishRelationFilter = m_publishRelationFilter.filterType(CmsRelationType.EMBEDDED_IMAGE);
            m_publishRelationFilter = m_publishRelationFilter.filterType(CmsRelationType.XML_STRONG);
        }
        return m_publishRelationFilter;
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
