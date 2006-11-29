/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishManager.java,v $
 * Date   : $Date: 2006/11/29 15:04:09 $
 * Version: $Revision: 1.1.2.1 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This manager provide access to the publish engine runtime information.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.5.5
 */
public class CmsPublishManager {

    /** The underlying publish engine. */
    private final CmsPublishEngine m_publishEngine;

    /**
     * Default constructor.<p>
     * 
     * @param engine the underlying publish engine
     */
    public CmsPublishManager(CmsPublishEngine engine) {

        m_publishEngine = engine;
    }

    /**
     * Aborts the given publish job.<p>
     * 
     * @param cms the cms context
     * @param publishJob the publish job to abort
     * 
     * @throws CmsException if there is some problem during unlocking the resources
     * @throws CmsSecurityException if the current user has not enough permissions 
     * @throws CmsPublishException if the publish job can not been aborted 
     */
    public void abortPublishJob(CmsObject cms, CmsPublishJobEnqueued publishJob) throws CmsException, CmsSecurityException, CmsPublishException {

        if (!cms.hasRole(CmsRole.PROJECT_MANAGER)
            && !cms.getRequestContext().currentUser().getName().equals(publishJob.getUserName())) {
            // Can only be executed by somebody with the role CmsRole#PROJECT_MANAGER or the owner of the job
            throw new CmsSecurityException(Messages.get().container(
                Messages.ERR_PUBLISH_ENGINE_ABORT_DENIED_1,
                cms.getRequestContext().currentUser().getName()));
        }
        m_publishEngine.abortPublishJob(cms.getRequestContext().currentUser().getName(), publishJob);
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
     * Returns the queue with still waiting publish jobs.<p>
     * 
     * @return a list of {@link CmsPublishJobEnqueued} objects
     */
    public List getPublishQueue() {

        return m_publishEngine.getPublishQueue().asList();
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
     * Removes the given publish listener.<p>
     * 
     * @param listener the publish listener to remove
     */
    public void removePublishListener(I_CmsPublishEventListener listener) {

        m_publishEngine.removePublishListener(listener);
    }

    /**
     * Waits until no publish jobs remain.<p>
     */
    public void waitWhileRunning() {

        // wait until it is done
        synchronized (this) {
            while (isRunning()) {
                try {
                    this.wait(1000); // wait a sec
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }
}
