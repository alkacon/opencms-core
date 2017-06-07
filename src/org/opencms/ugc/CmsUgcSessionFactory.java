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

package org.opencms.ugc;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ugc.shared.CmsUgcConstants;
import org.opencms.ugc.shared.CmsUgcException;
import org.opencms.util.CmsUUID;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Factory to create the form editing sessions.<p>
 */
public class CmsUgcSessionFactory {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUgcSessionFactory.class);

    /** The factory instance. */
    private static CmsUgcSessionFactory INSTANCE;

    /** The session queues. */
    private ConcurrentHashMap<CmsUUID, CmsUgcSessionQueue> m_queues = new ConcurrentHashMap<CmsUUID, CmsUgcSessionQueue>();

    /**
     * Constructor.<p>
     */
    private CmsUgcSessionFactory() {

    }

    /**
     * Returns the factory instance.<p>
     *
     * @return the factory instance
     */
    public static synchronized CmsUgcSessionFactory getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CmsUgcSessionFactory();
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
     * @throws CmsUgcException if creating the session fails
     */
    public CmsUgcSession createSession(CmsObject cms, HttpServletRequest request, CmsUgcConfiguration config)
    throws CmsUgcException {

        CmsUgcSession session = createSession(cms, config);
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
     * @throws CmsUgcException if creating the session fails
     */
    public CmsUgcSession createSession(CmsObject cms, HttpServletRequest request, String sitePath)
    throws CmsUgcException {

        CmsUgcConfigurationReader reader = new CmsUgcConfigurationReader(cms);
        CmsUgcConfiguration config = null;
        try {
            CmsFile configFile = cms.readFile(sitePath);
            config = reader.readConfiguration(configFile);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsUgcException(e, CmsUgcConstants.ErrorCode.errConfiguration, e.getLocalizedMessage());
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
     * @throws CmsUgcException if something goes wrong
     */
    public CmsUgcSession createSessionForFile(
        CmsObject cms,
        HttpServletRequest request,
        String configPath,
        String fileName) throws CmsUgcException {

        CmsUgcSession session = createSession(cms, request, configPath);
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
    public CmsUgcSession getSession(HttpServletRequest request, CmsUUID sessionId) {

        return (CmsUgcSession)request.getSession(true).getAttribute("" + sessionId);
    }

    /**
     * Creates a new editing session.<p>
     *
     * @param cms the cms context
     * @param config the configuration
     *
     * @return the form session
     *
     * @throws CmsUgcException if the session creation fails
     */
    private CmsUgcSession createSession(CmsObject cms, CmsUgcConfiguration config) throws CmsUgcException {

        if (getQueue(config).waitForSlot()) {
            try {
                return new CmsUgcSession(CmsUgcModuleAction.getAdminCms(), cms, config);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new CmsUgcException(e);
            }
        } else {
            String message = Messages.get().container(Messages.ERR_WAIT_QUEUE_EXCEEDED_0).key(
                cms.getRequestContext().getLocale());
            throw new CmsUgcException(CmsUgcConstants.ErrorCode.errMaxQueueLengthExceeded, message);
        }
    }

    /**
     * Returns the session queue.<p>
     *
     * @param config the form configuration
     *
     * @return the queue
     */
    private CmsUgcSessionQueue getQueue(CmsUgcConfiguration config) {

        CmsUgcSessionQueue queue = m_queues.get(config.getId());
        if (queue == null) {
            queue = CmsUgcSessionQueue.createQueue(config);
            m_queues.put(config.getId(), queue);
        } else {
            queue.updateFromConfiguration(config);
        }
        return queue;
    }
}
