/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsPropertyDialogSelector.java,v $
 * Date   : $Date: 2003/09/12 10:01:54 $
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
package org.opencms.workplace;

import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.main.OpenCms;

/**
 * Selects the property dialog which should be displayed by OpenCms.<p>
 * 
 * You can define the class of your property handler in the OpenCms registry.xml. 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/property_html
 * </ul>
 *
 * @see org.opencms.workplace.I_CmsPropertyDialogHandler
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.1
 */
public class CmsPropertyDialogSelector {
    
    private CmsJspActionElement m_jsp;
    private String m_paramResource;               
       
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPropertyDialogSelector(CmsJspActionElement jsp) {      
        setJsp(jsp);
        setParamResource(jsp.getRequest().getParameter(I_CmsWpConstants.C_PARA_RESOURCE));         
    }
    
    
    /**
     * Returns the uri of the property dialog which will be displayed.<p>
     *  
     * @return the uri of the property dialog
     */
    public String getPropertyDialogUri() {          
        try {
            // get the handler class from the OpenCms runtime property
            I_CmsPropertyDialogHandler propertyClass = (I_CmsPropertyDialogHandler)OpenCms.getRuntimeProperty("propertydialoghandler");
            // get the dialog URI from the class defined in the registry 
            return propertyClass.getPropertyDialogUri(getParamResource(), getJsp());
        } catch (Throwable t) {
            return CmsProperty.URI_PROPERTY_DIALOG;   
        }             
    } 
    
    /**
     * Returns the CmsJspActionElement.<p>
     * 
     * @return the CmsJspActionElement
     */
    private CmsJspActionElement getJsp() {
        return m_jsp;
    }
    
    /**
     * Sets the CmsJspActionElement.<p>
     * 
     * @param jsp the CmsJspActionElement
     */
    private void setJsp(CmsJspActionElement jsp) {
        m_jsp = jsp;
    }
    
    /**
     * Returns the resource parameter String.<p>
     * 
     * @return the resource parameter String
     */
    private String getParamResource() {
        return m_paramResource;
    }
    
    /**
     * Sets the resource parameter String.<p>
     * 
     * @param resource the resource parameter String
     */
    private void setParamResource(String resource) {
        m_paramResource = resource;
    }
    
}
