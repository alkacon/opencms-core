/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminVfsLinkManagement.java,v $
 * Date   : $Date: 2003/03/04 17:18:33 $
 * Version: $Revision: 1.1 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;

import java.util.Hashtable;

/**
 * Workplace class for the Check Project / Check Filesystem Links backoffice item.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
 */
public final class CmsAdminVfsLinkManagement extends CmsWorkplaceDefault implements I_CmsConstants {
    
    /**
     * Internal debugging flag.
     */
    public static final boolean DEBUG = false;

    private static final String C_JOIN_VFS_LINK_TARGETS_THREADNAME = "JOIN_VFS_LINK_TARGETS_THREAD";

    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        CmsXmlWpTemplateFile templateDocument = (CmsXmlWpTemplateFile) this.getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String action = (String) parameters.get("action");
        CmsAdminVfsLinkManagementThread vfsLinkManagementThread = null;
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();

        if ("start".equals(action)) {
            vfsLinkManagementThread = new CmsAdminVfsLinkManagementThread(cms, session);
            vfsLinkManagementThread.start();

            session.putValue(CmsAdminVfsLinkManagement.C_JOIN_VFS_LINK_TARGETS_THREADNAME, (CmsAdminVfsLinkManagementThread) vfsLinkManagementThread);

            templateDocument.setData("data", "");
            templateDocument.setData("endMethod", "");
            templateDocument.setData("autoUpdate", templateDocument.getDataValue("autoUpdate"));
            templateDocument.setData("text", lang.getLanguageValue("project.linkmanagement.message1"));
        } else if ("working".equals(action)) {
            vfsLinkManagementThread = (CmsAdminVfsLinkManagementThread) session.getValue(CmsAdminVfsLinkManagement.C_JOIN_VFS_LINK_TARGETS_THREADNAME);

            if (vfsLinkManagementThread.isAlive()) {
                templateDocument.setData("endMethod", "");
                templateDocument.setData("autoUpdate", templateDocument.getDataValue("autoUpdate"));
            } else {
                templateDocument.setData("endMethod", templateDocument.getDataValue("endMethod"));
                templateDocument.setData("autoUpdate", "");
                templateDocument.setData("text", lang.getLanguageValue("project.linkmanagement.message2"));

                session.removeValue(CmsAdminVfsLinkManagement.C_JOIN_VFS_LINK_TARGETS_THREADNAME);
            }

            templateDocument.setData("data", vfsLinkManagementThread.getReportUpdate());
        }

        return startProcessing(cms, templateDocument, elementName, parameters, templateSelector);
    }

}
