
/*
* File   : $File$
* Date   : $Date: 2001/06/22 16:01:33 $
* Version: $Revision: 1.5 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
        CmsRequestContext reqCont = m_cms.getRequestContext();
        try {

            // create a Project to delete the module.
            CmsProject project = m_cms.createProject("DeleteModule", "A System generated Project to delete The Module " + m_moduleName, C_GROUP_ADMIN, C_GROUP_ADMIN, ""+C_PROJECT_TYPE_TEMPORARY);
            reqCont.setCurrentProject(project.getId());

            // copy the resources to the project
            for(int i = 0;i < m_projectFiles.size();i++) {
                m_cms.copyResourceToProject((String)m_projectFiles.elementAt(i));
            }
            m_registry.deleteModule(m_moduleName, m_exclusion);

            // now publish the project
            m_cms.publishProject(project.getId());
        }
        catch(CmsException e) {
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }
}
