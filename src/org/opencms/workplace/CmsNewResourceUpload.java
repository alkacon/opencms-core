/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsNewResourceUpload.java,v $
 * Date   : $Date: 2004/03/18 16:13:59 $
 * Version: $Revision: 1.1 $
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

import org.opencms.file.CmsResourceTypeXmlPage;
import org.opencms.file.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * The new resource upload dialog handles the upload of single files or zipped files.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/newresource_upload.html
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.3
 */
public class CmsNewResourceUpload extends CmsNewResource {
    
    
    /** The value for the resource name form action */
    public static final int ACTION_NEWFORM2 = 120;
    /** The value for the resource name form submission action */
    public static final int ACTION_SUBMITFORM2 = 130;  
    
    /** The name for the resource form submission action */
    public static final String DIALOG_SUBMITFORM2 = "submitform2";
    
    /** Request parameter name for the upload file unzip flag */
    public static final String PARAM_UNZIPFILE = "unzipfile";
    /** Request parameter name for the upload file name */
    public static final String PARAM_UPLOADFILE = "uploadfile";
    /** Request parameter name for the new resource file name */
    public static final String PARAM_NEWRESOURCENAME = "newresourcename";
   
    private String m_paramUploadFile;
    private String m_paramUnzipFile;
    private String m_paramNewResourceName;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceUpload(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceUpload(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
        } else if (DIALOG_SUBMITFORM2.equals(getParamAction())) {
            setAction(ACTION_SUBMITFORM2);  
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for new resource dialog     
            setParamTitle(key("title.upload"));
        }      
    }
    
    /**
     * Uploads the specified file and unzips it, if selected.<p>
     */
    public void actionUpload() {
        // do the upload...
        setParamResource("test.gif");
        setParamNewResourceName("test.gif");
    }
    
    /**
     * Updates the file type and renames the file if desired
     */
    public void actionUpdateFile() {
        // renames file and changes file type...
    }
    
    /**
     * Builds the list of possible types for the uploaded file.<p>
     * 
     * @return the list of possible files for the uploaded resource
     */
    public String buildTypeList() {
        StringBuffer result = new StringBuffer(512);
        List resTypes = getCms().getAllResourceTypes();
        Iterator i = resTypes.iterator();
        while (i.hasNext()) {
            I_CmsResourceType resType = (I_CmsResourceType)i.next();
            int resTypeId = resType.getResourceType();
            if (resTypeId == CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID) {
                continue;
            }
            // get explorer type settings for current resource type
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getEplorerTypeSetting(resType.getResourceTypeName());
            if (settings != null) {
                result.append("<tr><td>");
                result.append("<input type=\"radio\" name=\"" + PARAM_NEWRESOURCETYPE + "\" value=\"" + settings.getName() + "\"></td>");
                result.append("\t<td><img src=\"" + getSkinUri() + "filetypes/" + settings.getIcon() + "\" border=\"0\" title=\"" + key(settings.getKey()) + "\"></td>\n");
                result.append("<td>" + key(settings.getKey()));
                result.append("</td></tr>\n");
            }
        }
        return result.toString();
    }
    
    /**
     * Returns the upload file name.<p>
     * 
     * @return the upload file name
     */
    public String getParamUploadFile() {
        return m_paramUploadFile;
    }

    /**
     * Sets the upload file name.<p>
     * 
     * @param uploadFile the upload file name
     */
    public void setParamUploadFile(String uploadFile) {
        m_paramUploadFile = uploadFile;
    }

    /**
     * Returns true if the upload file should be unzipped, otherwise false.<p>
     * 
     * @return true if the upload file should be unzipped, otherwise false
     */
    public String getParamUnzipFile() {
        return m_paramUnzipFile;
    }

    /**
     * Sets if the upload file should be unzipped.<p>
     * 
     * @param unzipFile true if the upload file should be unzipped
     */
    public void setParamUnzipFile(String unzipFile) {
        m_paramUnzipFile = unzipFile;
    }
    
    /**
     * Returns if the upload file should be unzipped.<p>
     * 
     * @return true if the upload file should be unzipped, otherwise false
     */
    public boolean unzipUpload() {
        return Boolean.valueOf(getParamUnzipFile()).booleanValue();
    }

    /**
     * Returns the new resource name of the uploaded file.<p>
     * 
     * @return the new resource name of the uploaded file
     */
    public String getParamNewResourceName() {
        return m_paramNewResourceName;
    }

    /**
     * Sets the new resource name of the uploaded file.<p>
     * 
     * @param newResourceName the new resource name of the uploaded file
     */
    public void setParamNewResourceName(String newResourceName) {
        m_paramNewResourceName = newResourceName;
    }

}
