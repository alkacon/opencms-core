/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/I_CmsEditorActionHandler.java,v $
 * Date   : $Date: 2004/10/22 15:53:58 $
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
package org.opencms.workplace.editors;

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Provides a method for performing an individual action if the user pressed a special button in the editor.<p>
 * 
 * You can define the class of your own editor action method in the OpenCms registry.xml by
 * changing the &lt;class&gt; subnode of the system node &lt;editoraction&gt; to another value. 
 * The class you enter must implement this interface to perform the editor action.<p>  
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.3.0
 */
public interface I_CmsEditorActionHandler {
    
    /** Prefix for direct edit end elements, used on JPS pages that supply the direct edit html. */
    String C_DIRECT_EDIT_AREA_END = "end_directedit";
    
    /** Prefix for direct edit start elements, used on JPS pages that supply the direct edit html. */
    String C_DIRECT_EDIT_AREA_START = "start_directedit";
    
    /** Key to identify the direct edit configuration file. */
    String C_DIRECT_EDIT_INCLUDE_FILE_URI = "__directEditIncludeFileUri";

    /** Default direct edit include file URI. */
    String C_DIRECT_EDIT_INCLUDE_FILE_URI_DEFAULT = "/system/workplace/editors/direct_edit.jsp";

    /** Element name for direct edit includes. */
    String C_DIRECT_EDIT_INCLUDES = "directedit_includes";
    
    /** Constant for: direct edit mode disabled. */
    String C_DIRECT_EDIT_MODE_DISABLED = "disabled";
    
    /** Constant for: direct edit mode enabled. */
    String C_DIRECT_EDIT_MODE_ENABLED = "enabled";
    
    /** Constant for: direct edit mode inactive. */
    String C_DIRECT_EDIT_MODE_INACTIVE = "inactive";
    
    /** Key to identify the edit button style, used on JPS pages that supply the direct edit html. */  
    String C_DIRECT_EDIT_PARAM_BUTTONSTYLE = "__directEditButtonStyle";
    
    /** Key to identify the edit element, used on JPS pages that supply the direct edit html. */  
    String C_DIRECT_EDIT_PARAM_ELEMENT = "__directEditElement";
    
    /** Key to identify the edit language, used on JPS pages that supply the direct edit html. */  
    String C_DIRECT_EDIT_PARAM_LOCALE = "__directEditLocale";
    
    /** Key to identify the edit target, used on JPS pages that supply the direct edit html. */
    String C_DIRECT_EDIT_PARAM_TARGET = "__directEditTarget";
    
    /** Key to identify additional direct edit options, used e.g. to control which direct edit buttons are displayed */
    String C_DIRECT_EDIT_PARAM_OPTIONS = "__directEditOptions";
    
    /** Key to identify the link to use for the "new" button (if enabled). */
    String C_DIRECT_EDIT_PARAM_NEWLINK = "__directEditNewLink";
    
    /** Option value that indicates the "edit" button should be displayed. */
    String C_DIRECT_EDIT_OPTION_EDIT = "edit";

    /** Option value that indicates the "delete" button should be displayed. */
    String C_DIRECT_EDIT_OPTION_DELETE = "delete";

    /** Option value that indicates the "new" button should be displayed. */
    String C_DIRECT_EDIT_OPTION_NEW = "new";
            
    /**
     * Performs an action which is configurable in the implementation of the interface, e.g. save, exit, publish.<p>
     * 
     * @param editor the current editor instance
     * @param jsp the JSP action element
     * @throws IOException if a redirection fails
     * @throws JspException if including a JSP fails
     */
    void editorAction(CmsEditor editor, CmsJspActionElement jsp) throws IOException, JspException;
    
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
     * Checks the current edit mode.<p>
     * The mode is used to select the appropriate elements for displaying the direct edit button.
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
     * @param element of the desired element or <code>null</code>
     * @param req the current request
     * 
     * @return the current edit mode ( null | inactive | disabled | enabled )
     */
    String getEditMode(CmsObject cmsObject, String filename, String element, ServletRequest req);
    
    /**
     * Returns true if the customized button should be active, otherwise false.<p>
     * 
     * @param jsp the JSP action element
     * @param resourceName the name of the edited resource
     * @return true if the customized button should be active, otherwise false
     */
    boolean isButtonActive(CmsJspActionElement jsp, String resourceName);

}
