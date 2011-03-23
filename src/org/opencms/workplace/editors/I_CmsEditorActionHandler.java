/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/I_CmsEditorActionHandler.java,v $
 * Date   : $Date: 2011/03/23 14:51:44 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.editors.directedit.CmsDirectEditButtonSelection;
import org.opencms.workplace.editors.directedit.CmsDirectEditJspIncludeProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditPermissions;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * Provides a method for performing an individual action if the user pressed a special button in the editor.<p>
 * 
 * You can define the class of your own editor action method in the OpenCms XML configuration files.
 * The class you enter must implement this interface to perform the editor action.<p>  
 * 
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.16 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsEditorActionHandler {

    /** 
     * Prefix for direct edit end elements, used on JPS pages that supply the direct edit html.
     * 
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_AREA_END}
     */
    String DIRECT_EDIT_AREA_END = CmsDirectEditJspIncludeProvider.DIRECT_EDIT_AREA_END;

    /** 
     * Prefix for direct edit start elements, used on JPS pages that supply the direct edit html. 
     * 
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_AREA_END}
     */
    String DIRECT_EDIT_AREA_START = CmsDirectEditJspIncludeProvider.DIRECT_EDIT_AREA_START;

    /** 
     * Key to identify the direct edit configuration file. 
     * 
     * @deprecated not longer used (the file URI is not longer stored in the page context)
     */
    String DIRECT_EDIT_INCLUDE_FILE_URI = "__directEditIncludeFileUri";

    /** 
     * Default direct edit include file URI. 
     * 
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_INCLUDE_FILE_URI_DEFAULT}
     */
    String DIRECT_EDIT_INCLUDE_FILE_URI_DEFAULT = CmsDirectEditJspIncludeProvider.DIRECT_EDIT_INCLUDE_FILE_URI_DEFAULT;

    /** 
     * Element name for direct edit includes.
     *  
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_INCLUDES}
     */
    String DIRECT_EDIT_INCLUDES = "directedit_includes";

    /** 
     * Constant for: direct edit mode disabled.
     * 
     * @deprecated use {@link CmsDirectEditPermissions#VALUE_DISABLED} or better {@link CmsDirectEditPermissions#DISABLED}
     */
    String DIRECT_EDIT_MODE_DISABLED = CmsDirectEditPermissions.VALUE_DISABLED;

    /** 
     * Constant for: direct edit mode enabled. 
     * 
     * @deprecated use {@link CmsDirectEditPermissions#VALUE_ENABLED} or better {@link CmsDirectEditPermissions#ENABLED}     
     */
    String DIRECT_EDIT_MODE_ENABLED = CmsDirectEditPermissions.VALUE_ENABLED;

    /** 
     * Constant for: direct edit mode inactive. 
     * 
     * @deprecated use {@link CmsDirectEditPermissions#VALUE_INACTIVE} or better {@link CmsDirectEditPermissions#INACTIVE}     
     */
    String DIRECT_EDIT_MODE_INACTIVE = CmsDirectEditPermissions.VALUE_INACTIVE;

    /** 
     * Option value that indicates the "delete" button should be displayed. 
     * 
     * @deprecated use {@link CmsDirectEditButtonSelection#VALUE_DELETE}    
     */
    String DIRECT_EDIT_OPTION_DELETE = CmsDirectEditButtonSelection.VALUE_DELETE;

    /** 
     * Option value that indicates the "edit" button should be displayed. 
     * 
     * @deprecated use {@link CmsDirectEditButtonSelection#VALUE_EDIT} or better {@link CmsDirectEditButtonSelection#EDIT}
     */
    String DIRECT_EDIT_OPTION_EDIT = CmsDirectEditButtonSelection.VALUE_EDIT;

    /** 
     * Option value that indicates the "new" button should be displayed.
     * 
     * @deprecated use {@link CmsDirectEditButtonSelection#VALUE_NEW}
     */
    String DIRECT_EDIT_OPTION_NEW = CmsDirectEditButtonSelection.VALUE_NEW;

    /** 
     * Key to identify the edit button style, used on JPS pages that supply the direct edit html.
     * 
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_PARAM_BUTTONSTYLE}
     */
    String DIRECT_EDIT_PARAM_BUTTONSTYLE = CmsDirectEditJspIncludeProvider.DIRECT_EDIT_PARAM_BUTTONSTYLE;

    /** 
     * Key to identify the edit element, used on JPS pages that supply the direct edit html. 
     * 
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_PARAM_ELEMENT}
     */
    String DIRECT_EDIT_PARAM_ELEMENT = CmsDirectEditJspIncludeProvider.DIRECT_EDIT_PARAM_ELEMENT;

    /** 
     * Key to identify the edit language, used on JPS pages that supply the direct edit html. 
     * 
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_PARAM_LOCALE}
     */
    String DIRECT_EDIT_PARAM_LOCALE = CmsDirectEditJspIncludeProvider.DIRECT_EDIT_PARAM_LOCALE;

    /** 
     * Key to identify the link to use for the "new" button (if enabled). 
     * 
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_PARAM_NEWLINK}
     */
    String DIRECT_EDIT_PARAM_NEWLINK = CmsDirectEditJspIncludeProvider.DIRECT_EDIT_PARAM_NEWLINK;

    /** 
     * Key to identify additional direct edit options, used e.g. to control which direct edit buttons are displayed 
     * 
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_PARAM_OPTIONS}
     */
    String DIRECT_EDIT_PARAM_OPTIONS = CmsDirectEditJspIncludeProvider.DIRECT_EDIT_PARAM_OPTIONS;

    /** 
     * Key to identify the edit target, used on JPS pages that supply the direct edit html. 
     * 
     * @deprecated use {@link CmsDirectEditJspIncludeProvider#DIRECT_EDIT_PARAM_TARGET}
     */
    String DIRECT_EDIT_PARAM_TARGET = CmsDirectEditJspIncludeProvider.DIRECT_EDIT_PARAM_TARGET;

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
     * Returns true if the customized button should be active, otherwise false.<p>
     * 
     * @param jsp the JSP action element
     * @param resourceName the name of the edited resource
     * @return true if the customized button should be active, otherwise false
     */
    boolean isButtonActive(CmsJspActionElement jsp, String resourceName);
}