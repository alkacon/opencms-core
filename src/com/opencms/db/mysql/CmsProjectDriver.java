/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/mysql/Attic/CmsProjectDriver.java,v $
 * Date   : $Date: 2003/05/23 16:26:46 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 
package com.opencms.db.mysql;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsTaskLog;

import java.util.Vector;

/**
 * This is the generic access module to load and store resources from and into
 * the database.
 *
 * @version $Revision: 1.3 $ $Date: 2003/05/23 16:26:46 $
 * @since 5.1.2
 */
public class CmsProjectDriver extends com.opencms.db.generic.CmsProjectDriver {

    /**
     * Deletes all properties for a project.
     *
     * @param project The project to delete.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProjectProperties(CmsProject project) throws CmsException {
        // get all resources of the project
        Vector resources = m_driverManager.getVfsDriver().readResources(project);
        for (int i = 0; i < resources.size(); i++) {
            // delete the properties for each resource in project
            m_driverManager.getVfsDriver().deleteAllProperties(project.getId(),(CmsResource) resources.elementAt(i));
        }
    }

    /**
     * Destroys this access-module
     * @throws throws CmsException if something goes wrong.
     */
    public void destroy() throws CmsException {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[" + this.getClass().getName() + "] destroyed!");
        }
    }

    /**
     * retrieve the correct instance of the queries holder.
     * This method should be overloaded if other query strings should be used.
     */
    public com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new com.opencms.db.mysql.CmsSqlManager(dbPoolUrl);
    }
    
    /**
     * Reads log entries for a project with
     * added unescaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param project The projec for tasklog to read.
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readProjectLogs(int projectid) throws CmsException {
        Vector v = super.readProjectLogs(projectid);
        for (int i=0; i<v.size(); i++) {
            CmsTaskLog log = (CmsTaskLog)v.elementAt(i);
            log.setComment(CmsSqlManager.escape(log.getComment()));
            v.set(i, log);
        }
        return v;    
    }       
    
}
