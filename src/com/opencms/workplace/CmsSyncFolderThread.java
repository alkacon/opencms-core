
/*
* File   : $File$
* Date   : $Date: 2001/02/05 16:57:27 $
* Version: $Revision: 1.2 $
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
 * Thread for create a new project.
 *
 * @author: Edna Falkenhan
 */

public class CmsSyncFolderThread extends Thread implements I_CmsConstants {

    private Vector m_folders;

    private CmsObject m_cms;

    private boolean m_newProject;

    /**
     * Insert the method's description here.
     *
     */

    public CmsSyncFolderThread(CmsObject cms, Vector folders, boolean newProject) {
        m_cms = cms;
        m_folders = folders;
        m_newProject = newProject;
    }

    public void run() {
        I_CmsSession session = m_cms.getRequestContext().getSession(true);
        try {
            // synchronize the resource
            for(int i = 0;i < m_folders.size();i++) {
                // if a new project was created for synchronisation, copy the resource to the project
                if (m_newProject){
                    m_cms.copyResourceToProject((String)m_folders.elementAt(i));
                    CmsFolder folder = m_cms.readFolder((String)m_folders.elementAt(i));
                }
		m_cms.syncFolder((String)m_folders.elementAt(i));
            }
        }
        catch(CmsException e) {
            session.putValue(C_SESSION_THREAD_ERROR, Utils.getStackTrace(e));
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }
}