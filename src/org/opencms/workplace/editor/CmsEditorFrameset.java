/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditorFrameset.java,v $
 * Date   : $Date: 2004/02/04 17:18:07 $
 * Version: $Revision: 1.15 $
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
import com.opencms.flex.jsp.CmsJspActionElement;

import org.opencms.locale.CmsEncoder;
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
 * @version $Revision: 1.15 $
 * 
 * @since 5.1.12
 */
public class CmsEditorFrameset extends CmsEditor {
    
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
            retValue.append(paramName + "=" + CmsEncoder.encode(paramValue));
            if (paramNames.hasMoreElements()) {
                retValue.append("&");
            }
        }
        return retValue.toString();
    }
    
    /**
     * Deletes the temporary file and unlocks the edited resource when in direct edit mode.<p>
     * 
     * This method is needed in the editor close help frame, which is called when the user presses
     * the "back" button or closes the browser window when editing a page.<p>
     * 
     * @param forceUnlock if true, the resource will be unlocked anyway
     */
    public void actionClear(boolean forceUnlock) {
        // delete the temporary file        
        deleteTempFile();
        if ("true".equals(getParamDirectedit()) || forceUnlock) {
            // unlock the resource when in direct edit mode or force unlock is true
            try {
                getCms().unlockResource(getParamResource(), false);
            } catch (CmsException e) {
                // ignore this exception
            }
        }
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
        // return emtpy String, has to be implemented
        return "";
    }
    
    /**
     * @see org.opencms.workplace.editor.CmsEditor#initContent()
     */
    protected final void initContent() {
        // do nothing, has to be implemented
    }
    
}
