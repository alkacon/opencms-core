/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditorHandler.java,v $
 * Date   : $Date: 2004/02/04 15:48:16 $
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
package org.opencms.workplace.editor;

import com.opencms.core.CmsException;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypeJsp;
import com.opencms.file.CmsResourceTypeNewPage;
import com.opencms.file.CmsResourceTypePlain;
import com.opencms.file.CmsResourceTypeXMLTemplate;
import com.opencms.file.CmsResourceTypeXmlPage;
import com.opencms.flex.jsp.CmsJspActionElement;

/**
 * This editor handler class returns the editor URI depending on various factors.<p>
 * 
 * Editor selection criteria:
 * <ul>
 * <li>resource type</li>
 * <li>the users browser</li>
 * </ul>
 * 
 * @see org.opencms.workplace.editor.I_CmsEditorHandler 
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.3.1
 */
public class CmsEditorHandler implements I_CmsEditorHandler {
    
    /**
     * Default constructor needed for editor handler implementation.<p>
     */
    public CmsEditorHandler() {
        // empty constructor
    }
    
    /**
     * @see org.opencms.workplace.editor.I_CmsEditorHandler#getEditorUri(java.lang.String, com.opencms.flex.jsp.CmsJspActionElement)
     */
    public String getEditorUri(String resource, CmsJspActionElement jsp) {
        // first try to get the "edit as text" and "noactivex" parameters from the request
        String editAsText = jsp.getRequest().getParameter("editastext");
        String noActiveX = jsp.getRequest().getParameter("noactivex");
        // initialize resource type with -1 (unknown resource type)
        int resTypeId = -1;
        if ("true".equals(editAsText)) {
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
            if (CmsEditor.BROWSER_NS.equals(CmsEditor.getBrowserType(jsp.getCmsObject()))) {
                return CmsEditor.C_PATH_EDITORS + "simplehtml/editor.html";
            } else {
                return CmsEditor.C_PATH_EDITORS + "msdhtml/editor.html";
            }    
            
        case CmsResourceTypeJsp.C_RESOURCE_TYPE_ID:
        case CmsResourceTypePlain.C_RESOURCE_TYPE_ID:
        case CmsResourceTypeXMLTemplate.C_RESOURCE_TYPE_ID:
        default:
            // resource is text or xml type, return ledit editor or simple text editor
            if (CmsEditor.BROWSER_IE.equals(CmsEditor.getBrowserType(jsp.getCmsObject())) && !"true".equals(noActiveX)) {
                return CmsEditor.C_PATH_EDITORS + "ledit/editor.html";
            } else {
                return CmsEditor.C_PATH_EDITORS + "simple/editor.html";
            }
            
        }
    }    
    
}
