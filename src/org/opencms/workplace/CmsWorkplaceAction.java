/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceAction.java,v $
 * Date   : $Date: 2004/08/19 11:26:32 $
 * Version: $Revision: 1.27 $
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
 
package org.opencms.workplace;

import org.opencms.main.OpenCms;

import org.opencms.file.CmsObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utility class with static methods, mainly intended for the transition of
 * functionality from the old XML based workplace to the new JSP workplace.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.27 $
 * 
 * @since 5.1
 */
public final class CmsWorkplaceAction { 
    
    /** Path to the different workplace views. */    
    public static final String C_PATH_VIEWS_WORKPLACE = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "views/";
    
    /** Path to the explorer workplace view. */    
    public static final String C_PATH_VIEW_EXPLORER = C_PATH_VIEWS_WORKPLACE + "explorer/";
    
    /** Path to the XML workplace. */    
    public static final String C_PATH_XML_WORKPLACE = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/";
   
    /** Path to the JSP workplace frame loader file. */    
    public static final String C_JSP_WORKPLACE_URI = C_PATH_VIEWS_WORKPLACE + "top_fs.jsp";
    
    /** Path to the XML workplace frame loader file. */    
    public static final String C_XML_WORKPLACE_URI = C_PATH_XML_WORKPLACE + "index.html";   
    
    /** File name of explorer file list loader (same for JSP and XML). */
    public static final String C_FILE_WORKPLACE_FILELIST = "explorer_files.jsp";
    
    /** Relative path to the JSP explorer. */
    public static final String C_JSP_WORKPLACE_FILELIST = "../views/explorer/" + C_FILE_WORKPLACE_FILELIST;
        
    /**
     * Constructor is private since there must be no instances of this class.<p>
     */
    private CmsWorkplaceAction() {
        // empty
    }
    
    /**
     * Updates the user preferences after changes have been made.<p>
     * 
     * @param cms the current cms context
     * @param req the current http request
     */
    public static void updatePreferences(CmsObject cms, HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_SETTINGS);
        if (settings == null) {
            return;
        }
        settings = CmsWorkplace.initWorkplaceSettings(cms, settings, true);    
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
            return C_XML_WORKPLACE_URI;
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_SETTINGS);
        if (settings == null) {
            return C_XML_WORKPLACE_URI;
        }
        return C_JSP_WORKPLACE_URI;        
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
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_SETTINGS);
        if (settings != null) {
            return settings.getExplorerResource();
        } else {
            return (String)session.getAttribute(I_CmsWpConstants.C_PARA_FILELIST);
        }
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
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_SETTINGS);
        if (settings != null) {
            settings.setExplorerResource(currentFolder);
        }       
        session.setAttribute(I_CmsWpConstants.C_PARA_FILELIST, currentFolder);       
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
            return I_CmsWpConstants.C_WP_EXPLORER_FILELIST;
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_SETTINGS);
        if (settings == null) {
            return I_CmsWpConstants.C_WP_EXPLORER_FILELIST;
        }
        return C_JSP_WORKPLACE_FILELIST;        
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
        String link = C_PATH_XML_WORKPLACE + C_FILE_WORKPLACE_FILELIST;
        if (session == null) {
            return OpenCms.getLinkManager().substituteLink(cms, link);
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_SETTINGS);
        if (settings == null) {
            return OpenCms.getLinkManager().substituteLink(cms, link);
        }
        return OpenCms.getLinkManager().substituteLink(cms, C_PATH_VIEW_EXPLORER + C_FILE_WORKPLACE_FILELIST);        
    } 
}
