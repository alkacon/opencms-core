/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/Messages.java,v $
 * Date   : $Date: 2005/05/20 09:13:46 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Achim Westermann (a.westermann@alkacon.com)
 * @since 5.7.3
 */
public final class Messages extends A_CmsMessageBundle {
    
    /** Message constant for key in the resource bundle. */
    public static final String ERR_GET_FILE_BYTES_1 = "ERR_GET_FILE_BYTES_1";
  
    /** Message constant for key in the resource bundle. */
    public static final String ERR_HTMLIMPORT_PARSE_1 = "ERR_HTMLIMPORT_PARSE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ELEM_RENAME_VALIDATE_ENTER_NEW_ELEM_0 = "GUI_ELEM_RENAME_VALIDATE_ENTER_NEW_ELEM_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_ELEM_RENAME_VALIDATE_ENTER_OLD_ELEM_0 = "GUI_ELEM_RENAME_VALIDATE_ENTER_OLD_ELEM_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ELEM_RENAME_VALIDATE_INVALID_NEW_ELEM_2 = "GUI_ELEM_RENAME_VALIDATE_INVALID_NEW_ELEM_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ELEM_RENAME_VALIDATE_RESOURCE_FOLDER_0 = "GUI_ELEM_RENAME_VALIDATE_RESOURCE_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ELEM_RENAME_VALIDATE_SELECT_LANGUAGE_0 = "GUI_ELEM_RENAME_VALIDATE_SELECT_LANGUAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ELEM_RENAME_VALIDATE_SELECT_TEMPLATE_0 = "GUI_ELEM_RENAME_VALIDATE_SELECT_TEMPLATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HTMLIMPORT_CONSTRAINT_OFFLINE_0 = "GUI_HTMLIMPORT_CONSTRAINT_OFFLINE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HTMLIMPORT_DESTDIR_1 = "GUI_HTMLIMPORT_DESTDIR_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HTMLIMPORT_DOWNGALLERY_1 = "GUI_HTMLIMPORT_DOWNGALLERY_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HTMLIMPORT_IMGGALLERY_1 = "GUI_HTMLIMPORT_IMGGALLERY_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HTMLIMPORT_INPUTDIR_1 = "GUI_HTMLIMPORT_INPUTDIR_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HTMLIMPORT_INVALID_ELEM_2 = "GUI_HTMLIMPORT_INVALID_ELEM_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HTMLIMPORT_LINKGALLERY_1 = "GUI_HTMLIMPORT_LINKGALLERY_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HTMLIMPORT_TEMPLATE_1 = "GUI_HTMLIMPORT_TEMPLATE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MERGE_PAGES_VALIDATE_FIRST_FOLDER_0 = "GUI_MERGE_PAGES_VALIDATE_FIRST_FOLDER_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_MERGE_PAGES_VALIDATE_FIRST_FOLDER_1 = "GUI_MERGE_PAGES_VALIDATE_FIRST_FOLDER_1";    
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_MERGE_PAGES_VALIDATE_SAME_FOLDER_0 = "GUI_MERGE_PAGES_VALIDATE_SAME_FOLDER_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_MERGE_PAGES_VALIDATE_SECOND_FOLDER_0 = "GUI_MERGE_PAGES_VALIDATE_SECOND_FOLDER_0";        
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_MERGE_PAGES_VALIDATE_SECOND_FOLDER_1 = "GUI_MERGE_PAGES_VALIDATE_SECOND_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RENAME_LANG_1 = "GUI_RENAME_LANG_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTMLIMPORT_CONVERSION_ERROR_0 = "LOG_HTMLIMPORT_CONVERSION_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTMLIMPORT_CONVERSION_ERROR_1 = "LOG_HTMLIMPORT_CONVERSION_ERROR_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.tools.content.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     * 
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     * 
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}