/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsNewResourceFolder.java,v $
 * Date   : $Date: 2004/12/03 15:06:45 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsPropertyAdvanced;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The new resource folder dialog handles the creation of a folder.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_folder.jsp
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.3.3
 */
public class CmsNewResourceFolder extends CmsNewResource {
    
    /** Request parameter name for the create index file flag. */
    public static final String PARAM_CREATEINDEX = "createindex";
    
    private String m_paramCreateIndex;
       
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceFolder(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceFolder(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
            setParamTitle(key("title.newfolder"));
        }      
    }
    
    /**
     * Redirects to the property dialog if the resourceeditprops parameter is true.<p>
     * 
     * If the parameter is not true, the dialog will be closed.<p>
     * 
     * @throws IOException if redirecting to the property dialog fails
     * @throws JspException if an inclusion fails
     */
    public void actionEditProperties() throws IOException, JspException {
        boolean editProps = Boolean.valueOf(getParamNewResourceEditProps()).booleanValue();
        boolean createIndex = Boolean.valueOf(getParamCreateIndex()).booleanValue();
        if (editProps || createIndex) {
            // edit properties checkbox checked, redirect to property dialog
            String params = "?" + PARAM_RESOURCE + "=" + CmsEncoder.encode(getParamResource());
            if (createIndex) {
                // set dialogmode to wizard - create index page to indicate the creation of the index page
                params += "&" + CmsPropertyAdvanced.PARAM_DIALOGMODE + "=" + CmsPropertyAdvanced.MODE_WIZARD_CREATEINDEX;
            } else {
                // set dialogmode to wizard
                params += "&" + CmsPropertyAdvanced.PARAM_DIALOGMODE + "=" + CmsPropertyAdvanced.MODE_WIZARD;
            }
            if (editProps) {
                // edit properties of folder, redirect to property dialog
                sendCmsRedirect(CmsPropertyAdvanced.URI_PROPERTY_DIALOG_HANDLER + params);
            } else if (createIndex) {
                // create an index file in the new folder, redirect to new xmlpage dialog              
                String newFolder = getParamResource();
                if (!newFolder.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                    newFolder += I_CmsConstants.C_FOLDER_SEPARATOR;
                }
                // set the current explorer resource to the new created folder
                getSettings().setExplorerResource(newFolder);

                String newUri = OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeXmlPage.C_RESOURCE_TYPE_NAME).getNewResourceUri();
                newUri += "?" + CmsPropertyAdvanced.PARAM_DIALOGMODE + "=" + CmsPropertyAdvanced.MODE_WIZARD_CREATEINDEX;
                try {
                    // redirect to new xmlpage dialog
                    sendCmsRedirect(C_PATH_DIALOGS + newUri);
                    return;
                } catch (Exception e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error redirecting to new xmlpage dialog " + C_PATH_DIALOGS + newUri);
                    }      
                }
            }
        } 
        // edit properties and create index file not checked, close the dialog and update tree
        List folderList = new ArrayList(1);
        folderList.add(CmsResource.getParentFolder(getParamResource()));
        getJsp().getRequest().setAttribute(C_REQUEST_ATTRIBUTE_RELOADTREE, folderList);
        actionCloseDialog();     
    }
    
    /**
     * Creates the folder using the specified resource name.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionCreateResource() throws JspException {
        try {
            // get the full resource name
            String fullResourceName = computeFullResourceName();
            // create the folder            
            getCms().createResource(fullResourceName, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);           
            setParamResource(fullResourceName);   
            setResourceCreated(true);
        } catch (CmsException e) {
            // error creating folder, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.newresource"));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }

    }

    /**
     * Returns the create index file parameter value.<p>
     * 
     * @return the create index file parameter value
     */
    public String getParamCreateIndex() {
        return m_paramCreateIndex;
    }

    /**
     * Sets the create index file parameter value.<p>
     * 
     * @param createIndex the create index file parameter value
     */
    public void setParamCreateIndex(String createIndex) {
        m_paramCreateIndex = createIndex;
    }

}
