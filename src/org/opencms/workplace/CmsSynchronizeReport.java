/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsSynchronizeReport.java,v $
 * Date   : $Date: 2004/01/06 17:06:05 $
 * Version: $Revision: 1.4 $
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

import org.opencms.importexport.CmsExport;
import org.opencms.security.CmsSecurityException;
import org.opencms.threads.CmsSynchronizeThread;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides an output window for a CmsReport.<p> 
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 * @since 5.1.10
 */
public class CmsSynchronizeReport extends CmsReport {
    
    /** The dialog type */
    public static final String DIALOG_TYPE = "sync";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSynchronizeReport(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSynchronizeReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }    
        
    /**
     * Performs the move report, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionReport() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_UPDATE:
                setParamAction(REPORT_UPDATE);   
                getJsp().include(C_FILE_REPORT_OUTPUT);  
                break;
            case ACTION_REPORT_BEGIN:
            case ACTION_CONFIRMED:
            default:
                Vector resources;
                try {
                    resources = getSynchronizeResources();
                } catch (CmsException e) {        
                    // show error dialog
                    setParamErrorstack(e.getStackTraceAsString());
                    setParamMessage(key("error.message." + getParamDialogtype()));
                    setParamReasonSuggestion(getErrorSuggestionDefault());
                    getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);    
                    break;                              
                }
                CmsSynchronizeThread thread = new CmsSynchronizeThread(getCms(), resources);
                setParamAction(REPORT_BEGIN);
                setParamThread(thread.getId().toString());
                getJsp().include(C_FILE_REPORT_OUTPUT);  
                break;
        }
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
        if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (REPORT_UPDATE.equals(getParamAction())) {
            setAction(ACTION_REPORT_UPDATE);         
        } else if (REPORT_BEGIN.equals(getParamAction())) {
            setAction(ACTION_REPORT_BEGIN);
        } else if (REPORT_END.equals(getParamAction())) {
            setAction(ACTION_REPORT_END);
        } else {                        
            setAction(ACTION_DEFAULT);
            // add the title for the dialog 
            setParamTitle(key("title.sync"));
        }                 
    }
    
    /**
     * Returns the list of resources to be synchronized from the OpenCms registry.<p>
     *  
     * @return the list of resources to be synchronized from the OpenCms registry
     * @throws CmsSecurityException in case the user has no write permissions for the project or resources to sync
     * @throws CmsException in case something goes wrong
     */
    private Vector getSynchronizeResources() throws CmsSecurityException, CmsException {        
        Vector folders = new Vector();
        Vector files   = new Vector();
        Hashtable resources;
        // first read the sync files from the registry
        resources = getCms().getRegistry().getSystemValues(I_CmsConstants.C_SYNCHRONISATION_RESOURCE);
        int count = resources.size();
        for (int i=0; i<count; i++) {
            String resource = (String)resources.get("res" + (i+1));
            if (CmsResource.isFolder(resource)) {
                folders.add(resource);
            } else {
                files.add(resource);
            }
        }
        // remove redundant resources
        CmsExport.checkRedundancies(folders, files);
        // combine the result 
        Vector result = new Vector(folders.size() + files.size());
        result.addAll(folders);
        result.addAll(files);
        List projectResources = getCms().readProjectResources(getCms().getRequestContext().currentProject());
        Iterator i = result.iterator();
        while (i.hasNext()) {
            String resource = (String)i.next();
            if (! getCms().hasPermissions(resource, I_CmsConstants.C_WRITE_ACCESS)) {
                // no write access to target folder
                throw new CmsSecurityException("No write permissions on resource: " + resource, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);                
            }
            if (! CmsProject.isInsideProject(projectResources, resource)) {
                throw new CmsSecurityException("Resource path not in current project: " + resource, CmsSecurityException.C_SECURITY_NO_PERMISSIONS);
            }
        }
        return result;        
    }
}
