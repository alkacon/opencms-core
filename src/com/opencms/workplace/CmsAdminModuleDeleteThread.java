/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleDeleteThread.java,v $
* Date   : $Date: 2001/09/06 13:21:44 $
* Version: $Revision: 1.10 $
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

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import java.io.*;

/**
 * Thread for deleting a module.
 * Creation date: (15.09.00 14:39:20)
 * @author: Hanjo Riege
 */
public class CmsAdminModuleDeleteThread extends Thread implements I_CmsConstants {
    private String m_moduleName;
    private Vector m_exclusion;
    private Vector m_projectFiles;
    private I_CmsRegistry m_registry;
    private CmsObject m_cms;

    /**
     * Insert the method's description here.
     * Creation date: (13.09.00 09:52:24)
     */
    public CmsAdminModuleDeleteThread(CmsObject cms, I_CmsRegistry reg, String moduleZipName, Vector exclusion, Vector projectFiles) {
        m_moduleName = moduleZipName;
        m_registry = reg;
        m_exclusion = exclusion;
        m_cms = cms;
        m_projectFiles = projectFiles;
    }
    public void run() {
         // Dont try to get the session this way in a thread!
         // It will result in a NullPointerException sometimes.
         // !I_CmsSession session = m_cms.getRequestContext().getSession(true);
        CmsRequestContext reqCont = m_cms.getRequestContext();
        String at = "createProject: ";
        try {

            // create a Project to delete the module.
            CmsProject project = m_cms.createProject("DeleteModule", "A System generated Project to delete The Module " + m_moduleName, C_GROUP_ADMIN, C_GROUP_ADMIN, C_PROJECT_TYPE_TEMPORARY);
            reqCont.setCurrentProject(project.getId());

            at = "copyResourceToProject: ";
            // copy the resources to the project
            for(int i = 0;i < m_projectFiles.size();i++) {
                m_cms.copyResourceToProject((String)m_projectFiles.elementAt(i));
            }
            at = "delete files: ";
            m_registry.deleteModule(m_moduleName, m_exclusion);

            at = "publishProject: ";
            // now publish the project
            m_cms.unlockProject(project.getId());
            m_cms.publishProject(project.getId());
        }
        catch(CmsException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, "delete module failed "+at + e.getMessage());
            }
        }
    }
}
