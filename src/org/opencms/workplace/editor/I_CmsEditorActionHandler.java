/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/I_CmsEditorActionHandler.java,v $
 * Date   : $Date: 2004/02/09 13:05:37 $
 * Version: $Revision: 1.6 $
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

import org.opencms.page.CmsXmlPage;

import com.opencms.file.CmsObject;
import com.opencms.flex.jsp.CmsJspActionElement;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * Provides a method for performing an individual action if the user pressed a special button in the editor.<p>
 * 
 * You can define the class of your own editor action method in the OpenCms registry.xml by
 * changing the &lt;class&gt; subnode of the system node &lt;editoraction&gt; to another value. 
 * The class you enter must implement this interface to perform the editor action.<p>  
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.6 $
 * 
 * @since 5.3.0
 */
public interface I_CmsEditorActionHandler {
    
    /** Constant for: edit mode enabled */
    String C_EDITMODE_ENABLED = "enabled";
    
    /** Constant for: edit mode disabled */
    String C_EDITMODE_DISABLED = "disabled";
    
    /** Constant for: edit mode inactive */
    String C_EDITMODE_INACTIVE = "inactive";
    
    /**
     * Prefix for edit area start elements
     */
    String C_EDIT_STARTAREA = "start_editarea";
    
    /**
     * Prefix for edit area end elements
     */
    String C_EDIT_ENDAREA = "end_editarea";

    /**
     * Key to identify the edit area attribute
     */
    String C_EDIT_AREA = "__editArea";
    
    /**
     * Key to identify the edit body
     */  
    String C_EDIT_BODY = "__editBody";
    
    /**
     * Key to identify the edit language
     */  
    String C_EDIT_LANGUAGE = "__editLanguage";
    
    /**
     * Key to identify the edit target
     */
    String C_EDIT_TARGET = "__editTarget";

    /**
     * Default editarea elements
     */
    String C_EDITAREA_DEFAULTS = "/system/workplace/jsp/editors/editarea.jsp";

    /**
     * Element name for editarea includes
     */
    String C_EDITAREA_INCLUDES = "editarea_includes";
    
    /**
     * Performs an action which is configurable in the implementation of the interface, e.g. save, exit, publish.<p>
     * 
     * @param editor the current editor instance
     * @param jsp the JSP action element
     * @throws IOException if a redirection fails
     * @throws JspException if including a JSP fails
     */
    void editorAction(CmsDefaultPageEditor editor, CmsJspActionElement jsp) throws IOException, JspException;
    
    /**
     * Returns the key name of the button displayed in the editor.<p>
     * 
     * @return the key name of the button
     */
    String getButtonName();
    
    /**
     * Returns the URL of the button displayed in the editor.<p>
     * 
     * @param jsp the JSP action element
     * @param resourceName the name of the edited resource
     * @return the URL of the button
     */
    String getButtonUrl(CmsJspActionElement jsp, String resourceName);
    
    /**
     * Returns true if the customized button should be active, otherwise false.<p>
     * 
     * @param jsp the JSP action element
     * @param resourceName the name of the edited resource
     * @return true if the customized button should be active, otherwise false
     */
    boolean isButtonActive(CmsJspActionElement jsp, String resourceName);
    
    /**
     * Checks the current edit mode.<p>
     * The mode is used to select the appropriate elements for displaying the edit area.
     * 
     * If the resource is displayed in online project -> editmode = null
     * If the resource is temporary -> editmode = inactive
     * If the resource does not belong to the current project -> editmode = inactive
     * If the current user has no write permissions on the resource -> editmode = inactive
     * If the resource is locked for another user -> editmode = disabled
     * Otherwise -> editmode = enabled
     * 
     * @param cmsObject the cms object
     * @param filename name of the resource
     * @param page the <code>CmsXmlPage</code> instance of the file or <code>null</code>
     * @param element of the desired element or <code>null</code>
     * @return the current edit mode ( null | inactive | disabled | enabled )
     */
    String getEditMode(CmsObject cmsObject, String filename, CmsXmlPage page, String element);

}
