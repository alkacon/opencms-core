
/*
* File   : $File$
* Date   : $Date: 2001/05/17 14:10:32 $
* Version: $Revision: 1.8 $
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
 * Creation date: (13.10.00 14:39:20)
 * @author: Hanjo Riege
 */

public class CmsAdminDatabaseExportThread extends Thread implements I_CmsConstants {

    private CmsObject m_cms;

    private String m_fileName;

    private String[] m_exportPaths;

    private boolean m_excludeSystem;

    private boolean m_excludeUnchanged;

    private boolean m_exportUserdata;

    private I_CmsSession m_session;

    /**
     * Insert the method's description here.
     * Creation date: (13.09.00 09:52:24)
     */

    public CmsAdminDatabaseExportThread(CmsObject cms, String fileName,
            String[] exportPaths, boolean excludeSystem, boolean excludeUnchanged,
            boolean exportUserdata, I_CmsSession session) {
        m_cms = cms;
        m_exportPaths = exportPaths;
        m_fileName = fileName;
        m_excludeSystem = excludeSystem;
        m_excludeUnchanged = excludeUnchanged;
        m_exportUserdata = exportUserdata;
        m_session = session;
    }

    public void run() {
        try {

            // do the export
            m_cms.exportResources(m_fileName, m_exportPaths, m_excludeSystem, m_excludeUnchanged, m_exportUserdata);
        }
        catch(CmsException e) {
            m_session.putValue(C_SESSION_THREAD_ERROR, Utils.getStackTrace(e));
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }
}
