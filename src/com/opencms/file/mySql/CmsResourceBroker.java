/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsResourceBroker.java,v $
* Date   : $Date: 2003/05/21 10:25:00 $
* Version: $Revision: 1.48 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file.mySql;

import com.opencms.core.CmsException;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsTask;
import com.opencms.file.CmsUser;

/**
 * This is THE resource broker. It merges all resource broker
 * into one public class. The interface is local to package. <B>All</B> methods
 * get additional parameters (callingUser and currentproject) to check the security-
 * police.
 * 
 * @author Andreas Schouten
 * @author Michaela Schleich
 * @author Michael Emmerich
 * @author Anders Fugmann
 * @version $Revision: 1.48 $ $Date: 2003/05/21 10:25:00 $
 */
public class CmsResourceBroker extends com.opencms.file.genericSql.CmsResourceBroker {
    
    /**
     * Reads a project from the Cms.
     * 
     * <B>Security</B>
     * All users are granted.
     * 
     * @param currentUser The user who requested this method.
     * @param currentProject The current project of the user.
     * @param task The task to read the project of.
     * 
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, CmsTask task) throws CmsException {
        // read the parent of the task, until it has no parents.
        while (task.getParent() != 0) {
            task = readTask(currentUser, currentProject, task.getParent());
        }
        return m_dbAccess.readProject(task);
    }
    
}
