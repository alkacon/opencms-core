/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditorFrameset.java,v $
 * Date   : $Date: 2004/01/09 08:30:37 $
 * Version: $Revision: 1.12 $
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
package org.opencms.workplace.editor;

import com.opencms.core.CmsException;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypeJsp;
import com.opencms.file.CmsResourceTypeNewPage;
import com.opencms.file.CmsResourceTypePlain;
import com.opencms.file.CmsResourceTypeXMLTemplate;
import com.opencms.file.CmsResourceTypeXmlPage;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;

import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class to create the editor frameset.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/editor_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.12 $
 * 
 * @since 5.1.12
 */
public class CmsEditorFrameset extends CmsEditor implements I_CmsEditorHandler {
    
    /**
     * Default constructor needed for editor handler implementation.<p>
     */
    public CmsEditorFrameset() {
        super(null);
    }
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditorFrameset(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
    }
    
    /**
     * @see org.opencms.workplace.editor.I_CmsEditorHandler#getEditorUri(java.lang.String, com.opencms.flex.jsp.CmsJspActionElement)
     */
    public String getEditorUri(String resource, CmsJspActionElement jsp) {
        // first try to get the "edit as text" and "noactivex" parameters from the request
        setParamEditastext(jsp.getRequest().getParameter("editastext"));
        setParamNoactivex(jsp.getRequest().getParameter("noactivex"));
        // initialize resource type with -1 (unknown resource type)
        int resTypeId = -1;
        if ("true".equals(getParamEditastext())) {
            // the resource should be treated as text, set the id
            resTypeId = CmsResourceTypePlain.C_RESOURCE_TYPE_ID;
        } else {
            try {
                // get the type of the edited resource
                CmsResource res = jsp.getCmsObject().readFileHeader(resource);
                resTypeId = res.getType();
            } catch (CmsException e) {
                // do nothing here
            }
        }
        
        switch (resTypeId) {
            
        case CmsResourceTypeNewPage.C_RESOURCE_TYPE_ID:
        case CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID:
            // resource is of type "xml page", show the dhtml control or simple page editor
            if (BROWSER_NS.equals(getBrowserType(jsp.getCmsObject()))) {
                return C_PATH_EDITORS + "simplehtml/editor.html";
            } else {
                return C_PATH_EDITORS + "msdhtml/editor.html";
            }    
            
        case CmsResourceTypeJsp.C_RESOURCE_TYPE_ID:
        case CmsResourceTypePlain.C_RESOURCE_TYPE_ID:
        case CmsResourceTypeXMLTemplate.C_RESOURCE_TYPE_ID:
        default:
            // resource is text or xml type, return ledit editor or simple text editor
            if (BROWSER_IE.equals(getBrowserType(jsp.getCmsObject())) && !"true".equals(getParamNoactivex())) {
                return C_PATH_EDITORS + "ledit/editor.html";
            } else {
                return C_PATH_EDITORS + "simple/editor.html";
            }
            
        }
    }
    
    /**
     * Returns all present request parameters as String.<p>
     * 
     * The String is formatted as a parameter String ("param1=val1&param2=val2") with UTF-8 encoded values.<p>
     * 
     * @return all present request parameters as String
     */
    public String getParamsAsRequest() {
        StringBuffer retValue = new StringBuffer(512);
        HttpServletRequest request = getJsp().getRequest();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String)paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            retValue.append(paramName + "=" + Encoder.encode(paramValue));
            if (paramNames.hasMoreElements()) {
                retValue.append("&");
            }
        }
        return retValue.toString();
    }
    
    /**
     * @see org.opencms.workplace.editor.CmsEditor#actionExit()
     */
    public final void actionExit() {
        // do nothing, has to be implemented
    }
    
    /**
     * @see org.opencms.workplace.editor.CmsEditor#actionSave()
     */
    public final void actionSave() { 
        // do nothing, has to be implemented
    }
    
    /**
     * @see org.opencms.workplace.editor.CmsEditor#getEditorResourceUri()
     */
    public final String getEditorResourceUri() {
        return "";
    }
    
    /**
     * @see org.opencms.workplace.editor.CmsEditor#initContent()
     */
    protected final void initContent() {
        //do nothing, has to be implemented
    }
    
}
