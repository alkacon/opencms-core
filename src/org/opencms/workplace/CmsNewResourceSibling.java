/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsNewResourceSibling.java,v $
 * Date   : $Date: 2004/06/28 07:47:32 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteManager;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The new resource sibling dialog handles the creation of a new sibling in the VFS.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/newresource_sibling.html
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.7 $
 * 
 * @since 5.3.3
 */
public class CmsNewResourceSibling extends CmsNewResourcePointer {
    
    /** Request parameter name for the keep properties flag. */
    public static final String PARAM_KEEPPROPERTIES = "keepproperties";
    
    private String m_paramKeepProperties;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceSibling(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceSibling(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }    
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);                            
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for new resource dialog     
            setParamTitle(key("title.newsibling"));
        }      
    }

    /**
     * Creates the new sibling of a resource.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionCreateResource() throws JspException {
        try {
            // create the full resource name
            String fullResourceName = computeFullResourceName();
            String newResourceParam = fullResourceName;
            // the target
            String targetName = getParamLinkTarget();
            if (targetName == null) {
                targetName = "";
            }
            
            // create the sibling                        
            boolean restoreSiteRoot = false;
            try {
                if (CmsSiteManager.getSiteRoot(targetName) != null) {
                    // add site root to new resource path
                    String siteRootFolder = getCms().getRequestContext().getSiteRoot();
                    if (siteRootFolder.endsWith("/")) {
                        siteRootFolder = siteRootFolder.substring(0, siteRootFolder.length()-1);
                    }  
                    fullResourceName = siteRootFolder + fullResourceName;
                    getCms().getRequestContext().saveSiteRoot();
                    getCms().getRequestContext().setSiteRoot("/");
                    restoreSiteRoot = true;
                }
                
                // check if the link target is a file or a folder
                boolean isFolder = false;               
                CmsResource targetRes = getCms().readResource(targetName);
                isFolder = targetRes.isFolder();                 
                
                if (isFolder) {                  
                    // link URL is a folder, so copy the folder with all sub resources as siblings
                    if (targetName.endsWith("/")) {
                        targetName = targetName.substring(0, targetName.length()-1);
                    }                    
                    // copy the folder
                    getCms().copyResource(targetName, fullResourceName, I_CmsConstants.C_COPY_AS_SIBLING);                                
                } else {                  
                    // link URL is a file, so create sibling of the link target
                    List targetProperties = null; 
                    boolean keepProperties = Boolean.valueOf(getParamKeepProperties()).booleanValue();
                    if (keepProperties) {
                        // keep the individual properties of the original file
                        try {
                            targetProperties = getCms().readPropertyObjects(targetName, false);
                        } catch (Exception e) {
                            OpenCms.getLog(this).error("Error reading properties of " + targetName, e);
                        }                
                    }
                    getCms().createSibling(targetName, fullResourceName, targetProperties);                   
                }
                
            } finally {
                // restore the site root
                if (restoreSiteRoot) {
                    getCms().getRequestContext().restoreSiteRoot();
                }
            }
            
            // set resource parameter to new resource name for property dialog
            setParamResource(newResourceParam);            
        } catch (CmsException e) {
            // error creating pointer, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.newlink"));
            setParamReasonSuggestion(key("error.reason.newlink") + "<br>\n" + key("error.suggestion.newlink") + "\n");
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }

    }
    
    /**
     * Returns the current explorer path for use in Javascript of new sibling dialog.<p>
     * 
     * @return the current explorer path
     */
    public String getCurrentPath() {
        String path = getSettings().getExplorerResource();
        if (path == null) {
            path = "/";
        }
        return CmsResource.getFolderPath(path);
    }

    /**
     * Returns the keep properties request parameter value.<p>
     * 
     * @return the keep properties request parameter value
     */
    public String getParamKeepProperties() {
        return m_paramKeepProperties;
    }

    /**
     * Sets the keep properties request parameter value.<p>
     * 
     * @param keepProperties the keep properties request parameter value
     */
    public void setParamKeepProperties(String keepProperties) {
        m_paramKeepProperties = keepProperties;
    }

}