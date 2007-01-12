/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/Attic/CmsWebdavLockException.java,v $
 * Date   : $Date: 2007/01/12 17:24:42 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.webdav;

import java.util.Hashtable;

/**
 * This exception handles errors while locking resources. If locking collections
 * it is possible to get more than one status (multi-status), so this exception
 * can handle a single and multiple errors while locking.
 * 
 * @author Peter Bonrad
 */
public class CmsWebdavLockException extends Exception {

    private static final long serialVersionUID = -2636181528251719992L;

    /** Specifies if this exception is a multi status exception. */
    protected boolean m_multiStatus;

    /** 
     * List of multiple status defined by this exception with path as key and status
     * as value.
     */
    protected Hashtable m_multiStatusList;

    /** The error status of this exception. */
    protected int m_status;

    /**
     * Creates a new LockException wich is not handled as multi status.
     *
     * @param status the error status of this exception
     */
    public CmsWebdavLockException(int status) {

        this(status, false);
    }

    /**
     * Creates a new LockException, either multistatus or not.
     * 
     * @param status the error status of this exception
     * @param multi is this Exception a multi status exception or not.
     */
    public CmsWebdavLockException(int status, boolean multi) {

        m_status = status;
        m_multiStatus = multi;
        m_multiStatusList = new Hashtable();
    }

    /**
     * Adds a new status to the multi status list.
     * 
     * @param href the path/href the status is for
     * @param status the status
     */
    public void addMultiStatus(String href, int status) {

        m_multiStatusList.put(href, status);
    }

    /**
     * Returns the multiStatusList.<p>
     *
     * @return the multiStatusList
     */
    public Hashtable getMultiStatusList() {

        return m_multiStatusList;
    }

    /**
     * Returns the status.<p>
     *
     * @return the status
     */
    public int getStatus() {

        return m_status;
    }

    /**
     * Returns the multiStatus.<p>
     *
     * @return the multiStatus
     */
    public boolean isMultiStatus() {

        return m_multiStatus;
    }

    /**
     * Sets the multiStatus.<p>
     *
     * @param multiStatus the multiStatus to set
     */
    public void setMultiStatus(boolean multiStatus) {

        m_multiStatus = multiStatus;
    }

    /**
     * Sets the multiStatusList.<p>
     *
     * @param multiStatusList the multiStatusList to set
     */
    public void setMultiStatusList(Hashtable multiStatusList) {

        m_multiStatusList = multiStatusList;
    }

    /**
     * Sets the status.<p>
     *
     * @param status the status to set
     */
    public void setStatus(int status) {

        m_status = status;
    }

}
