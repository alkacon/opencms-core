/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsNewResourceXmlContent.java,v $
 * Date   : $Date: 2004/12/03 15:06:45 $
 * Version: $Revision: 1.2 $
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The new resource xmlcontent dialog handles the creation of a xmlcontent.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_xmlcontent.jsp
 * </ul>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 */
public class CmsNewResourceXmlContent extends CmsNewResource {

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceXmlContent(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceXmlContent(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
        } else if (DIALOG_SUBMITFORM.equals(getParamAction())) {
            setAction(ACTION_SUBMITFORM);  
        } else if (DIALOG_NEWFORM.equals(getParamAction())) {
            setAction(ACTION_NEWFORM);
            setParamTitle(key("title.new" + getParamNewResourceType()));
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for new resource dialog     
            setParamTitle(key("title.new"));
        }      
    }
    
 
    
    /**
     * Creates the resource using the specified resource name and the newresourcetype parameter.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionCreateResource() throws JspException {
        try {
            // create the full resource name
            String fullResourceName = computeFullResourceName();
            // create the folder            
            getCms().createResource(
                fullResourceName, 
                OpenCms.getResourceManager().getResourceType(getParamNewResourceType()).getTypeId(), null, Collections.EMPTY_LIST);           
            setParamResource(fullResourceName); 
            setResourceCreated(true);
        } catch (CmsException e) {
            // error creating file, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.newresource"));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }
   
}
