/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/I_CmsRuntimeInfo.java,v $
 * Date   : $Date: 2004/10/22 15:03:26 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db;

import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;

/**
 * This interface defines a runtime info object.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $
 * @since 5.5.2
 * @see org.opencms.db.I_CmsRuntimeInfoFactory
 */
public interface I_CmsRuntimeInfo {

    /** Flag to collect vfs driver runtime infos. */
    int C_RUNTIMEINFO_VFS = 1;

    /** Flag to collect user driver runtime infos. */
    int C_RUNTIMEINFO_USER = 2;

    /** Flag to collect backup driver runtime infos. */
    int C_RUNTIMEINFO_BACKUP = 4;

    /** Flag to collect project driver runtime infos. */
    int C_RUNTIMEINFO_PROJECT = 8;

    /** Flag to collect workflow driver runtime infos. */
    int C_RUNTIMEINFO_WORKFLOW = 16;

    /** Flag to collect runtime infos of all drivers. */
    int C_RUNTIMEINFO_COMPLETE = C_RUNTIMEINFO_VFS
        + C_RUNTIMEINFO_USER
        + C_RUNTIMEINFO_BACKUP
        + C_RUNTIMEINFO_PROJECT
        + C_RUNTIMEINFO_WORKFLOW;

    /** Flag to collect runtime infos of the VFS and user drivers. */
    int C_RUNTIMEINFO_VFS_AND_USER = C_RUNTIMEINFO_VFS + C_RUNTIMEINFO_USER;

    /** Flag to collect runtime infos drivers being used to publish resources. */
    int C_RUNTIMEINFO_PUBLISH = C_RUNTIMEINFO_VFS
        + C_RUNTIMEINFO_USER
        + C_RUNTIMEINFO_BACKUP
        + C_RUNTIMEINFO_PROJECT;

    /**
     * Clears this runtime info.<p>
     */
    void clear();

    /**
     * Adds an object to this runtime info.<p>
     * 
     * @param object the object to be added
     * @return the added object
     */
    Object push(Object object);

    /**
     * Removes the last added object from this runtime info.<p>
     * 
     * @return the last added object
     * @throws CmsException if something goes wrong
     */
    Object pop() throws CmsException;

    /**
     * Prints an (error) message to the specified report.<p>
     * 
     * @param report a Cms report
     * @param message an error message
     * @param throwable a root cause in case of an error message
     */
    void report(I_CmsReport report, String message, Throwable throwable);

}