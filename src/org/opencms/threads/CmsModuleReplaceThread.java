/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsModuleReplaceThread.java,v $
 * Date   : $Date: 2004/02/13 13:45:33 $
 * Version: $Revision: 1.5 $
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

package org.opencms.threads;

import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.workplace.I_CmsWpConstants;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRegistry;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Replaces a module.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * @since 5.1.10
 */
public class CmsModuleReplaceThread extends A_CmsReportThread {

    private static final boolean DEBUG = false;
    private Vector m_conflictFiles;
    private A_CmsReportThread m_deleteThread;
    private A_CmsReportThread m_importThread;
    private String m_moduleName;
    private int m_phase;
    private Vector m_projectFiles;
    private CmsRegistry m_registry;
    private String m_reportContent = null;
    private String m_zipName;

    /**
     * Creates the module replace thread.<p>
     * 
     * @param cms the current cms context  
     * @param reg the registry to write the new module information to
     * @param zipName the name of the module ZIP file
     * @param moduleName the name of the module 
     * @param conflictFiles vector of conflict files 
     */
    public CmsModuleReplaceThread(
        CmsObject cms, 
        CmsRegistry reg, 
        String moduleName, 
        String zipName, 
        Vector conflictFiles    
    ) {
        super(cms, "OpenCms: Module replacement of " + moduleName);
        m_moduleName = moduleName;
        m_zipName = zipName;
        m_registry = reg;
        m_conflictFiles = conflictFiles;

        // add the module resources to the project files
        m_projectFiles = CmsModuleReplaceThread.getModuleResources(cms, reg, moduleName);

        m_deleteThread = new CmsModuleDeleteThread(getCms(), m_registry, m_moduleName, m_conflictFiles, m_projectFiles, true);
        m_importThread = new CmsModuleImportThread(getCms(), m_registry, m_moduleName, m_zipName, m_conflictFiles);
        if (DEBUG) {
            System.err.println("CmsAdminModuleReplaceThread() constructed");
        }
        m_phase = 0;
    }

    /**
     * Collects all resource names belonging to a module in a Vector.<p>
     * 
     * @param cms the CmsObject
     * @param reg the registry
     * @param moduleName the name of the module
     * @return Vector with path Strings of resources
     */
    public static Vector getModuleResources(CmsObject cms, CmsRegistry reg, String moduleName) {
        Vector resNames = new Vector();

        // add the module folder to the project resources
        resNames.add(I_CmsWpConstants.C_VFS_PATH_MODULES + moduleName + "/");

        if (reg.getModuleType(moduleName).equals(CmsRegistry.C_MODULE_TYPE_SIMPLE)) {
            // SIMPLE MODULE

            // check if additional resources outside the system/modules/{exportName} folder were 
            // specified as module resources by reading the property {C_MODULE_PROPERTY_ADDITIONAL_RESOURCES}
            // to the module (in the module administration)
            String additionalResources = OpenCms.getRegistry().getModuleParameterString(moduleName, I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES);
            StringTokenizer additionalResourceTokens = null;

            if (additionalResources != null && !additionalResources.equals("")) {
                // add each additonal folder plus its content folder under "content/bodys"
                additionalResourceTokens = new StringTokenizer(additionalResources, I_CmsConstants.C_MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR);

                // add each resource plus its equivalent at content/bodys to 
                // the string array of all resources for the export
                while (additionalResourceTokens.hasMoreTokens()) {
                    String currentResource = additionalResourceTokens.nextToken().trim();

                    if (!"-".equals(currentResource)) {
                        try {
                            // check if the resource exists and then add it to the Vector
                            cms.readFileHeader(currentResource);
                            resNames.add(currentResource);
                        } catch (CmsException e) {
                            // nothing we can do about this 
                        }
                    }
                }
            } else {
                // no additional resources were specified...
                return resNames;
            }
        } else {
            // TRADITIONAL MODULE
            return resNames;
        }
        return resNames;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {
        switch (m_phase) {
            case 1 :
                return m_deleteThread.getReportUpdate();
            case 2 :
                String content;
                if (m_reportContent != null) {
                    content = m_reportContent;
                    m_reportContent = null;
                } else {
                    content = "";
                }
                return content + m_importThread.getReportUpdate();
            default :
                // NOOP
        }
        return "";
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (DEBUG) {
            System.err.println("CmsAdminModuleReplaceThread() starting delete action ");
        }
        // phase 1: delete the existing module  
        m_phase = 1;
        m_deleteThread.run();
        // get remaining report contents
        m_reportContent = m_deleteThread.getReportUpdate();
        if (DEBUG) {
            System.err.println("CmsAdminModuleReplaceThread() starting import action ");
        }
        // phase 2: import the new module 
        m_phase = 2;
        m_importThread.run();
        if (DEBUG) {
            System.err.println("CmsAdminModuleReplaceThread() finished ");
        }
    }
}
