/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleImport.java,v $
* Date   : $Date: 2001/07/31 15:50:17 $
* Version: $Revision: 1.8 $
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
 * Thread for importing a module.
 * Creation date: (13.09.00 09:36:49)
 * @author: Hanjo Riege
 */
public class CmsAdminModuleImport extends Thread implements I_CmsConstants {
    private String m_moduleName;
    private Vector m_conflictFiles;
    private Vector m_projectFiles;
    private I_CmsRegistry m_registry;
    private CmsObject m_cms;

    /**
     * Insert the method's description here.
     * Creation date: (13.09.00 09:52:24)
     */
    public CmsAdminModuleImport(CmsObject cms, I_CmsRegistry reg, String moduleZipName, Vector conflictFiles, Vector projectFiles) {
        m_cms = cms;
        m_moduleName = moduleZipName;
        m_registry = reg;
        m_conflictFiles = conflictFiles;
        m_projectFiles = projectFiles;
    }
    public void run() {
        CmsRequestContext reqCont = m_cms.getRequestContext();
        String at = "ceateProject: ";
        try {

            // create a Project to import the module.
            CmsProject project = m_cms.createProject("ImportModule", "A System generated Project to import The Module " + m_moduleName, C_GROUP_ADMIN, C_GROUP_ADMIN, ""+C_PROJECT_TYPE_TEMPORARY);
            reqCont.setCurrentProject(project.getId());

            at = "copyResourcesToProject: ";
            // copy the resources to the project
            for(int i = 0;i < m_projectFiles.size();i++) {
                m_cms.copyResourceToProject((String)m_projectFiles.elementAt(i));
            }

            at = "import Data: ";
            // now import the module
            m_registry.importModule(m_moduleName, m_conflictFiles);

            at = "unlock project: ";
            // now unlock and publish the project
            m_cms.unlockProject(project.getId());
            at = "publishProject: ";
            m_cms.publishProject(project.getId());
        }
        catch(CmsException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, "import Module failed at "+at+ e.getMessage());
            }
        }
    }
}
