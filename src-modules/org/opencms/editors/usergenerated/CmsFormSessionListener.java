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

import org.opencms.main.CmsLog;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;

/**
 * Session listener for removing unused projects / resources from form editing sessions.<p> 
 */
public class CmsFormSessionListener implements HttpSessionListener {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormSessionListener.class);

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent se) {

        // ignore 
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent event) {

        HttpSession session = event.getSession();
        Enumeration<?> attrNames = session.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String)attrNames.nextElement();
            Object attribute = session.getAttribute(attrName);
            if (attribute instanceof CmsFormSession) {
                CmsFormSession formSession = (CmsFormSession)attribute;
                try {
                    formSession.onSessionDestroyed();
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);

                }
            }
        }

    }

}
