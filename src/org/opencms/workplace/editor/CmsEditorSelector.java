/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditorSelector.java,v $
 * Date   : $Date: 2004/02/13 13:45:33 $
 * Version: $Revision: 1.7 $
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


import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.workplace.I_CmsWpConstants;

/**
 * Selects the dialog which should be displayed by OpenCms depending on the registry value.<p>
 * 
 * You can define the class of your dialog handler in the OpenCms registry.xml in the &lt;dialoghandler&gt; node. 
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/editor_main_html
 * </ul>
 *
 * @see org.opencms.workplace.editor.I_CmsEditorHandler
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.7 $
 * 
 * @since 5.1.14
 */
public class CmsEditorSelector {
    
    // necessary member variables
    private CmsJspActionElement m_jsp;
    private String m_paramResource;
       
    /** Debug flag */
    public static final boolean C_DEBUG = false;      
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditorSelector(CmsJspActionElement jsp) {      
        setJsp(jsp);
        setParamResource(jsp.getRequest().getParameter(I_CmsWpConstants.C_PARA_RESOURCE));         
    }
    
    
    /**
     * Returns the uri of the dialog which will be displayed.<p>
     *  
     * @return the uri of the property dialog
     */
    public String getSelectedEditorUri() {          
        
        if (C_DEBUG) {
            System.err.println("[" + this.getClass().getName() + "].getSelectedEditorUri() - Resource: " + getParamResource());
        }
        // get the handler class from the OpenCms runtime property
        I_CmsEditorHandler editorClass = OpenCms.getWorkplaceManager().getEditorHandler();
        if (editorClass == null) {
            // error getting the dialog class, return to file list
            return I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "jsp/explorer_files.html";
        }
        // get the dialog URI from the class defined in the registry 
        return editorClass.getEditorUri(getParamResource(), getJsp());      
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
