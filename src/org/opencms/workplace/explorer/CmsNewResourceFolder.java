/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsNewResourceFolder.java,v $
 * Date   : $Date: 2005/10/11 09:39:29 $
 * Version: $Revision: 1.18.2.2 $
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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUriSplitter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsPropertyAdvanced;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
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
 * <p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.18.2.2 $ 
 * 
 * @since 6.0.0 
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
     * Creates the folder using the specified resource name.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionCreateResource() throws JspException {

        try {
            // calculate the new resource Title property value
            String title = computeNewTitleProperty();
            // get the full resource name
            String fullResourceName = computeFullResourceName();
            // create the Title and Navigation properties if configured
            List properties = createResourceProperties(
                fullResourceName,
                CmsResourceTypeFolder.getStaticTypeName(),
                title);
            // create the folder            
            getCms().createResource(fullResourceName, CmsResourceTypeFolder.getStaticTypeId(), null, properties);
            setParamResource(fullResourceName);
            setResourceCreated(true);
        } catch (Throwable e) {
            // error creating folder, show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_CREATE_FOLDER_0));
            includeErrorpage(this, e);
        }

    }

    /**
     * Forwards to the property dialog if the resourceeditprops parameter is true.<p>
     * 
     * If the parameter is not true, the dialog will be closed.<p>
     * 
     * @throws IOException if forwarding to the property dialog fails
     * @throws ServletException if forwarding to the property dialog fails
     * @throws JspException if an inclusion fails
     */
    public void actionEditProperties() throws IOException, JspException, ServletException {

        boolean editProps = Boolean.valueOf(getParamNewResourceEditProps()).booleanValue();
        boolean createIndex = Boolean.valueOf(getParamCreateIndex()).booleanValue();
        if (editProps) {
            // edit properties of folder, forward to property dialog
            Map params = new HashMap();
            params.put(PARAM_RESOURCE, getParamResource());
            if (createIndex) {
                // set dialogmode to wizard - create index page to indicate the creation of the index page
                params.put(CmsPropertyAdvanced.PARAM_DIALOGMODE, CmsPropertyAdvanced.MODE_WIZARD_CREATEINDEX);
            } else {
                // set dialogmode to wizard
                params.put(CmsPropertyAdvanced.PARAM_DIALOGMODE, CmsPropertyAdvanced.MODE_WIZARD);
            }
            sendForward(CmsPropertyAdvanced.URI_PROPERTY_DIALOG_HANDLER, params);
        } else if (createIndex) {
            // create an index file in the new folder, redirect to new xmlpage dialog              
            String newFolder = getParamResource();
            if (!newFolder.endsWith("/")) {
                newFolder += "/";
            }
            // set the current explorer resource to the new created folder
            getSettings().setExplorerResource(newFolder);
            String newUri = PATH_DIALOGS
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeXmlPage.getStaticTypeName()).getNewResourceUri();
            CmsUriSplitter splitter = new CmsUriSplitter(newUri);
            Map params = CmsRequestUtil.createParameterMap(splitter.getQuery());
            params.put(CmsPropertyAdvanced.PARAM_DIALOGMODE, CmsPropertyAdvanced.MODE_WIZARD_CREATEINDEX);
            sendForward(splitter.getPrefix(), params);
        } else {
            // edit properties and create index file not checked, close the dialog and update tree
            List folderList = new ArrayList(1);
            folderList.add(CmsResource.getParentFolder(getParamResource()));
            getJsp().getRequest().setAttribute(REQUEST_ATTRIBUTE_RELOADTREE, folderList);
            actionCloseDialog();
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

}
