/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleReplaceThread.java,v $
 * Date   : $Date: 2003/08/01 07:53:00 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
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
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.workplace;

import com.opencms.file.CmsObject;
import com.opencms.file.I_CmsRegistry;
import com.opencms.report.A_CmsReportThread;

import java.util.Vector;

/**
 * Replaces a module, showing a progress indicator report dialog that is continuously updated.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 * @since 5.0
 */
public class CmsAdminModuleReplaceThread extends A_CmsReportThread {

    private String m_moduleName;
    private String m_zipName;
    private Vector m_conflictFiles;
    private Vector m_projectFiles;
    private I_CmsRegistry m_registry;
    private CmsObject m_cms;
    private A_CmsReportThread m_deleteThread;
    private A_CmsReportThread m_importThread;
    private int m_phase;
    private String m_reportContent = null;
    
    /** DEBUG flag */
    private static final boolean DEBUG = false;    

    /**
     * Creates the module replace thread.
     * 
     * @param cms the current cms context  
     * @param reg the registry to write the new module information to
     * @param zipName the name of the module ZIP file
     * @param moduleName the name of the module 
     * @param conflictFiles vector of conflict files 
     */
    public CmsAdminModuleReplaceThread(CmsObject cms, I_CmsRegistry reg, String moduleName, String zipName, Vector conflictFiles) {
        super("OpenCms: Module replacement of " + moduleName);
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        m_moduleName = moduleName;
        m_zipName = zipName;
        m_registry = reg;
        m_conflictFiles = conflictFiles;
        m_deleteThread = new CmsAdminModuleDeleteThread(m_cms, m_registry, m_moduleName, m_conflictFiles, m_projectFiles, true);
        m_importThread = new CmsAdminModuleImportThread(m_cms, m_registry, m_moduleName, m_zipName, m_conflictFiles);
        if (DEBUG) System.err.println("CmsAdminModuleReplaceThread() constructed"); 
        m_phase = 0;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {      
            if (DEBUG) System.err.println("CmsAdminModuleReplaceThread() starting delete action ");
            // phase 1: delete the existing module  
            m_phase = 1;
            m_deleteThread.run();
            // get remaining report contents
            m_reportContent = m_deleteThread.getReportUpdate();
            if (DEBUG) System.err.println("CmsAdminModuleReplaceThread() starting import action ");
            // phase 2: import the new module 
            m_phase = 2;
            m_importThread.run();
            if (DEBUG) System.err.println("CmsAdminModuleReplaceThread() finished "); 
    }

    /**
     * Returns the part of the report that is ready.
     * 
     * @return the part of the report that is ready
     */
    public String getReportUpdate() {
        switch (m_phase) {
            case 1:
                return m_deleteThread.getReportUpdate();
            case 2:
                String content;
                if (m_reportContent != null) {
                    content = m_reportContent;  
                    m_reportContent = null;
                } else {
                    content = "";
                }
                return content + m_importThread.getReportUpdate();
            default:
                // NOOP
        }
        return "";
    }
}
