/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDbContext.java,v $
 * Date   : $Date: 2011/03/23 14:50:31 $
 * Version: $Revision: 1.22 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.db;

import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsException;
import org.opencms.flex.CmsFlexRequestContextInfo;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps context information to access the OpenCms database.<p> 
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.22 $
 * 
 * @since 6.0.0
 */
public class CmsDbContext {

    /** The current Flex request context info (if available). */
    protected CmsFlexRequestContextInfo m_flexRequestContextInfo;

    /** The id of the project for the context. */
    protected CmsUUID m_projectId;

    /** The wrapped user request context. */
    protected CmsRequestContext m_requestContext;

    /** Context attributes. */
    protected Map m_attributes;

    /**
     * Creates a new, empty database context.<p> 
     */
    public CmsDbContext() {

        this(null);
    }

    /**
     * Creates a new database context initialized with the given user request context.<p>
     *  
     * @param context the current users request context
     */
    public CmsDbContext(CmsRequestContext context) {

        m_requestContext = context;
        m_projectId = CmsUUID.getNullUUID();

        if (m_requestContext != null) {
            m_flexRequestContextInfo = (CmsFlexRequestContextInfo)m_requestContext.getAttribute(CmsRequestUtil.HEADER_LAST_MODIFIED);
        }
    }

    /**
     * Get an attribute from the dbc.<p>
     * @param key the attrbute key
     * @return the attribute value or null if the attribute does not exist
     */
    public Object getAttribute(String key) {

        if (m_attributes == null) {
            return null;
        }
        return m_attributes.get(key);
    }

    /**
     * Sets an attribute in the dbc.<p>
     * @param key the attribute key
     * @param value the attribute value
     */
    public void setAttribute(String key, Object value) {

        if (m_attributes == null) {
            m_attributes = new HashMap(4);
        }
        m_attributes.put(key, value);
    }

    /**
     * Clears this database context.<p>
     */
    public void clear() {

        m_requestContext = null;
        m_flexRequestContextInfo = null;
    }

    /**
     * Returns the current users project.<p>
     * 
     * @return the current users project
     */
    public CmsProject currentProject() {

        return m_requestContext.currentProject();
    }

    /**
     * Returns the current user.<p>
     * 
     * @return the current user
     */
    public CmsUser currentUser() {

        return m_requestContext.currentUser();
    }

    /**
     * Returns the current Flex request context info.<p>
     * 
     * @return the current Flex request context info
     */
    public CmsFlexRequestContextInfo getFlexRequestContextInfo() {

        return m_flexRequestContextInfo;
    }

    /**
     * Gets the history driver associated with this database context.<p>
     * 
     * @param projectId the project id for which the history driver should be retrieved
     * 
     * @return the history driver 
     */
    public I_CmsHistoryDriver getHistoryDriver(CmsUUID projectId) {

        return null;
    }

    /**
     * Gets the project driver associated with this database context.<p>
     * 
     * @param projectId the project id for which the project driver should be retrieved
     * 
     * @return the project driver 
     */
    public I_CmsProjectDriver getProjectDriver(CmsUUID projectId) {

        return null;
    }

    /**
     * Returns the project id of the context.<p>
     *
     * @return the project
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

    /**
     * Returns the request context.<p>
     *
     * @return the request context
     */
    public CmsRequestContext getRequestContext() {

        return m_requestContext;
    }

    /**
     * Gets the user driver associated with this database context.<p> 
     * 
     * @param projectId the project id for which the user driver should be retrieved 
     * 
     * @return the user driver 
     */
    public I_CmsUserDriver getUserDriver(CmsUUID projectId) {

        return null;
    }

    /**
     * Gets the VFS driver associated with this database context.<p> 
     * 
     * @param projectId the project id for which the VFS driver should be retrieved
     *  
     * @return the VFS driver 
     */
    public I_CmsVfsDriver getVfsDriver(CmsUUID projectId) {

        return null;
    }

    /**
     * Returns true if the database context uses the default implementation.<p>
     * 
     * @return true if the database context uses the default implementation
     */
    public boolean isDefaultDbContext() {

        return true;
    }

    /**
     * Processes the current database context.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void pop() throws CmsException {

        if (!isDefaultDbContext()) {
            throw new CmsException(Messages.get().container(Messages.ERR_PROCESS_DB_CONTEXT_0));
        }
    }

    /**
     * Removes the current site root prefix from the absolute path in the resource name,
     * that is adjusts the resource name for the current site root.<p> 
     * 
     * If no user request context is available, the given resource name is
     * returned unchanged.<p>
     * 
     * @param resourcename the resource name
     * 
     * @return the resource name adjusted for the current site root
     */
    public String removeSiteRoot(String resourcename) {

        if ((m_requestContext != null) && (resourcename != null)) {
            return m_requestContext.removeSiteRoot(resourcename);
        }

        return resourcename;
    }

    /**
     * Reports an error to the given report (if available) and to the OpenCms log file.<p>
     *  
     * @param report the report to write the error to
     * @param message the message to write to the report / log
     * @param throwable the exception to write to the report / log
     * 
     * @throws CmsException if the throwable parameter is not null and a CmsException
     * @throws CmsVfsException if the throwable parameter is not null and no CmsException
     */
    public void report(I_CmsReport report, CmsMessageContainer message, Throwable throwable)
    throws CmsVfsException, CmsException {

        if (report != null) {
            if (message != null) {
                report.println(message, I_CmsReport.FORMAT_ERROR);
            }
            if (throwable != null) {
                report.println(throwable);
            }
        }

        throwException(message, throwable);
    }

    /**
     * Sets the project id of the context.<p>
     *
     * @param projectId the id of the project to set
     */
    public void setProjectId(CmsUUID projectId) {

        m_projectId = projectId;
    }

    /**
     * Returns an Exception of the same type as throwable if throwable is an OpenCms Exception
     * with the message as a MessageContainer and the throwable as a cause.<p>
     * 
     * @param message the MessageContainer for the Exception to create
     * @param throwable the cause of the exception
     * 
     * @throws CmsVfsException if throwable is not an OpenCms Exception
     * @throws CmsException of the same type as throwable if throwable is an OpenCms Exception
     */
    void throwException(CmsMessageContainer message, Throwable throwable) throws CmsVfsException, CmsException {

        if (throwable instanceof CmsException) {
            throw ((CmsException)throwable).createException(message, throwable);
        } else if (throwable instanceof CmsRuntimeException) {
            throw ((CmsRuntimeException)throwable).createException(message, throwable);
        } else {
            throw new CmsVfsException(message, throwable);
        }
    }
}