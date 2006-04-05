/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsWorkplaceAction.java,v $
 * Date   : $Date: 2005/06/27 23:22:07 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.workplace;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsFrameset;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utility class with static methods, mainly intended for the transition of
 * functionality from the old XML based workplace to the new JSP workplace.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsWorkplaceAction {

    /** File name of explorer file list loader (same for JSP and XML). */
    public static final String FILE_WORKPLACE_FILELIST = "explorer_files.jsp";

    /** Relative path to the JSP explorer. */
    public static final String JSP_WORKPLACE_FILELIST = "../views/explorer/" + FILE_WORKPLACE_FILELIST;

    /** Path to the different workplace views. */
    public static final String PATH_VIEWS_WORKPLACE = CmsWorkplace.VFS_PATH_WORKPLACE + "views/";
    
    /** Path to the explorer workplace view. */
    public static final String PATH_VIEW_EXPLORER = PATH_VIEWS_WORKPLACE + "explorer/";

    /** Path to the XML workplace. */
    public static final String PATH_XML_WORKPLACE = CmsWorkplace.VFS_PATH_WORKPLACE + "action/";

    /** Path to the XML workplace frame loader file. */
    public static final String XML_WORKPLACE_URI = PATH_XML_WORKPLACE + "index.html";

    /** Session attribute for the file list. */
    private static final String FILELIST_ATTRIBUTE = "__filelist";

    /**
     * Constructor is private since there must be no instances of this class.<p>
     */
    private CmsWorkplaceAction() {

        // empty
    }

    /**
     * Returns the folder currently selected in the explorer.<p>
     * 
     * @param req the current http request
     * @return the folder currently selected in the explorer
     */
    public static String getCurrentFolder(HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        if (settings != null) {
            return settings.getExplorerResource();
        } else {
            return (String)session.getAttribute(FILELIST_ATTRIBUTE);
        }
    }

    /**
     * Returns the full uri (absoluth path with servlet context) of the file explorer.<p>
     * 
     * @param cms the current cms context
     * @param req the current http request
     * @return the uri of the file explorer
     */
    public static String getExplorerFileFullUri(CmsObject cms, HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        String link = PATH_XML_WORKPLACE + FILE_WORKPLACE_FILELIST;
        if (session == null) {
            return OpenCms.getLinkManager().substituteLink(cms, link);
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        if (settings == null) {
            return OpenCms.getLinkManager().substituteLink(cms, link);
        }
        return OpenCms.getLinkManager().substituteLink(cms, PATH_VIEW_EXPLORER + FILE_WORKPLACE_FILELIST);
    }

    /**
     * Returns the uri of the file explorer.<p>
     * 
     * @param req the current http request
     * @return the uri of the file explorer
     */
    public static String getExplorerFileUri(HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            return CmsWorkplaceDefault.C_WP_EXPLORER_FILELIST;
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        if (settings == null) {
            return CmsWorkplaceDefault.C_WP_EXPLORER_FILELIST;
        }
        return JSP_WORKPLACE_FILELIST;
    }

    /**
     * Returns the uri of the top workplace frameset.<p>
     * 
     * @param req the current http request
     * @return the uri of the top workplace frameset
     */
    public static String getWorkplaceUri(HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            return XML_WORKPLACE_URI;
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        if (settings == null) {
            return XML_WORKPLACE_URI;
        }
        return CmsFrameset.JSP_WORKPLACE_URI;
    }

    /**
     * Sets the folder currently selected in the explorer.<p>
     * 
     * @param req the current http request
     * @param currentFolder the folder to set
     */
    public static void setCurrentFolder(String currentFolder, HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        if (settings != null) {
            settings.setExplorerResource(currentFolder);
        }
        session.setAttribute(FILELIST_ATTRIBUTE, currentFolder);
    }
}
