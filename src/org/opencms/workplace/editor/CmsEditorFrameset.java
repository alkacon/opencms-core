/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditorFrameset.java,v $
 * Date   : $Date: 2003/11/21 16:21:58 $
 * Version: $Revision: 1.2 $
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
import com.opencms.file.CmsResourceTypeNewPage;
import com.opencms.flex.jsp.CmsJspActionElement;

import org.opencms.workplace.CmsWorkplaceSettings;

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
 * @version $Revision: 1.2 $
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
     * Returns the URI of the editor which will be used for the selected resource.<p>
     * 
     * The returned URI depends on the selected resource type, the used browser and the users preferences.<p>
     * 
     * @return URI of the editor which will be used for the selected resource
     */
    public String getEditorUri() {
        try {
            CmsResource res = getCms().readFileHeader(getParamResource());
            if (res.getType() == CmsResourceTypeNewPage.C_RESOURCE_TYPE_ID) {
                // resource a of type "new page", show the dhtml control
                return C_PATH_EDITORS + "msdhtml/editor.html";
            } else {
                // resource is text or xml style, return ledit editor or simple text editor
                if (BROWSER_IE.equals(getBrowserType())) {
                    return C_PATH_EDITORS + "ledit/editor.html";
                } else {
                    return C_PATH_EDITORS + "simple/editor.html";
                }
            }
        } catch (CmsException e) {
            // do nothing here
        }
        // return default (text) editor
        if (BROWSER_IE.equals(getBrowserType())) {
            return C_PATH_EDITORS + "ledit/editor.html";
        } else {
            return C_PATH_EDITORS + "simple/editor.html";
        }
    }
    
    /**
     * @see org.opencms.workplace.editor.CmsEditor#actionSave()
     */
    public void actionSave() { }
    
}
