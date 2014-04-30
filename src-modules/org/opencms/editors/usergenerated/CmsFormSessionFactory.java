/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.editors.usergenerated;

import org.opencms.editors.usergenerated.shared.CmsFormConstants;
import org.opencms.editors.usergenerated.shared.CmsFormException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Factory to create the form editing sessions.<p>
 */
public class CmsFormSessionFactory {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormSessionFactory.class);

    /** The factory instance. */
    private static CmsFormSessionFactory INSTANCE;

    /** The session queues. */
    private ConcurrentHashMap<CmsUUID, CmsSessionQueue> m_queues = new ConcurrentHashMap<CmsUUID, CmsSessionQueue>();

    /**
     * Constructor.<p>
     */
    private CmsFormSessionFactory() {

    }

    /**
     * Returns the factory instance.<p>
     * 
     * @return the factory instance
     */
    public static synchronized CmsFormSessionFactory getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CmsFormSessionFactory();
        }
        return INSTANCE;
    }

    /**
     * Creates a new editing session.<p>
     * 
     * @param cms the cms context
     * @param request the request
     * @param config the configuration
     * 
     * @return the form session
     * 
     * @throws CmsFormException if creating the session fails
     */
    public CmsFormSession createSession(CmsObject cms, HttpServletRequest request, CmsFormConfiguration config)
    throws CmsFormException {

        CmsFormSession session = createSession(cms, config);
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("" + session.getId(), session);
        return session;
    }

    /**
     * Creates a new editing session.<p>
     * 
     * @param cms the cms context
     * @param request the request
     * @param sitePath the configuration site path
     * 
     * @return the form session
     * 
     * @throws CmsFormException if creating the session fails
     */
    public CmsFormSession createSession(CmsObject cms, HttpServletRequest request, String sitePath)
    throws CmsFormException {

        CmsFormConfigurationReader reader = new CmsFormConfigurationReader(cms);
        CmsFormConfiguration config = null;
        try {
            CmsFile configFile = cms.readFile(sitePath);
            config = reader.readConfiguration(configFile);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsFormException(e, CmsFormConstants.ErrorCode.errConfiguration, e.getLocalizedMessage());
        }
        return createSession(cms, request, config);
    }

    /**
     * Creates a new session for a given file.<p>
     * 
     * @param cms the CMS context to use 
     * @param request the current request 
     * @param configPath the path of the form configuration 
     * @param fileName the file name (*not* path) of the XML content for which the session should be initialized
     *   
     * @return the newly created session 
     * @throws CmsFormException if something goes wrong 
     */
    public CmsFormSession createSessionForFile(
        CmsObject cms,
        HttpServletRequest request,
        String configPath,
        String fileName) throws CmsFormException {

        CmsFormSession session = createSession(cms, request, configPath);
        session.loadXmlContent(fileName);

        // when we open a session for existing files, we do not want them to get automatically deleted
        session.disableCleanup();
        return session;
    }

    /**
     * Returns the session, if already initialized.<p>
     * 
     * @param request the request
     * @param sessionId the form session id 
     * 
     * @return the session
     */
    public CmsFormSession getSession(HttpServletRequest request, CmsUUID sessionId) {

        return (CmsFormSession)request.getSession(true).getAttribute("" + sessionId);
    }

    /**
     * Creates a new editing session.<p>
     * 
     * @param cms the cms context
     * @param config the configuration
     * 
     * @return the form session
     * 
     * @throws CmsFormException if the session creation fails 
     */
    private CmsFormSession createSession(CmsObject cms, CmsFormConfiguration config) throws CmsFormException {

        if (getQueue(config).waitForSlot()) {
            try {
                return new CmsFormSession(CmsFormModuleAction.getAdminCms(), cms, config);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new CmsFormException(e);
            }
        } else {
            String message = Messages.get().container(Messages.ERR_WAIT_QUEUE_EXCEEDED_0).key(
                cms.getRequestContext().getLocale());
            throw new CmsFormException(CmsFormConstants.ErrorCode.errMaxQueueLengthExceeded, message);
        }
    }

    /**
     * Returns the session queue.<p>
     * 
     * @param config the form configuration
     * 
     * @return the queue
     */
    private CmsSessionQueue getQueue(CmsFormConfiguration config) {

        CmsSessionQueue queue = m_queues.get(config.getId());
        if (queue == null) {
            queue = CmsSessionQueue.createQueue(config);
            m_queues.put(config.getId(), queue);
        } else {
            queue.updateFromConfiguration(config);
        }
        return queue;
    }
}
