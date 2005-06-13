/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsCopyToProject.java,v $
 * Date   : $Date: 2005/06/13 10:17:21 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace.commons;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the copy to project dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/copytoproject.jsp
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 6.0
 */
public class CmsCopyToProject extends CmsDialog {
    
    /** Value for the action: copy the resource to current project. */
    public static final int ACTION_COPYTOPROJECT = 100;
    
    /** The dialog type. */
    public static final String DIALOG_TYPE = "copytoproject";
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCopyToProject.class);
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsCopyToProject(CmsJspActionElement jsp) {

        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsCopyToProject(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    } 

    /**
     * Performs the copy to project action, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionCopyToProject() throws JspException {
        
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            // copy the resource to the current project
            getCms().copyResourceToProject(getParamResource());
            // close the dialog
            actionCloseDialog(); 
        } catch (Throwable e) {
            // error copying resource to project, include error page
            includeErrorpage(this, e);  
        }
    }
    
    /**
     * Returns the HTML containing project information and confirmation question for the JSP.<p>
     * 
     * @return the HTML containing project information and confirmation question for the JSP
     */
    public String buildProjectInformation() {

        StringBuffer result = new StringBuffer(16);
        
        try {
            String[] localizedObject = new String[]{getCms().getRequestContext().currentProject().getName()};
            List resources = getCms().readProjectResources(getCms().getRequestContext().currentProject());
            Iterator i = resources.iterator();
            result.append(dialogBlockStart(key(Messages.GUI_COPYTOPROJECT_RESOURCES_0)));
            if (resources.size() > 0) {
                // at least one resource in current project
                result.append(key(Messages.GUI_COPYTOPROJECT_PART_1, localizedObject));
                result.append("<ul style=\"margin-top: 3px; margin-bottom: 3px;\">\n");
                while (i.hasNext()) {
                    // create resource list
                    result.append("\t<li>");
                    result.append((String)i.next());
                    result.append("</li>\n");
                }
                result.append("</ul>\n");
            } else {
                // no resources in current project
                result.append(key(Messages.GUI_COPYTOPROJECT_NOPART_1, localizedObject));
            }
            result.append(dialogBlockEnd());
            result.append(dialogSpacer());
        } catch (CmsException e) {
            // error reading project resources, should not happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        
        // determine resource name to show
        String resName = getParamResource();
        try {
            CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            if (res.isFolder() && !resName.endsWith("/")) {
                resName += "/";
            }
        } catch (CmsException e) {
            // error reading resource, should not happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        // show confirmation question
        String[] localizedObject = new String[]{resName, getCms().getRequestContext().currentProject().getName()};
        result.append(key(Messages.GUI_COPYTOPROJECT_PROJECT_CONFIRMATION_2, localizedObject));
        return result.toString();
    }
    
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        addMessages(Messages.get().getBundleName());
        super.initMessages();
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
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_COPYTOPROJECT);                            
        } else if (DIALOG_CANCEL.equals(getParamAction())) {          
            setAction(ACTION_CANCEL);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for copy to project dialog     
            setParamTitle(key(Messages.GUI_COPYTOPROJECT_TITLE_0));
        }      
    } 
    
}