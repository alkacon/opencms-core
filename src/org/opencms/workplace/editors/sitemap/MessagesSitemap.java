/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/sitemap/Attic/MessagesSitemap.java,v $
 * Date   : $Date: 2010/01/29 09:02:22 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editors.sitemap;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.6.0 
 */
public class MessagesSitemap extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SITEMAP_DELETE_SUBENTRIES_AND_LINKS_WARNING_0 = "GUI_SITEMAP_DELETE_SUBENTRIES_AND_LINKS_WARNING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SITEMAP_DELETE_SUBENTRIES_WARNING_0 = "GUI_SITEMAP_DELETE_SUBENTRIES_WARNING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SITEMAP_DELETE_LINKS_WARNING_0 = "GUI_SITEMAP_DELETE_LINKS_WARNING_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SITEMAP_EDIT_DIALOG_PATH_CANT_BE_EMPTY_0 = "ERR_SITEMAP_EDIT_DIALOG_PATH_CANT_BE_EMPTY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SITEMAP_LABEL_EDIT_DIALOG_PATH_0 = "GUI_SITEMAP_LABEL_EDIT_DIALOG_PATH_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String GUI_SITEMAP_ENTRY_NO_TITLE_0="GUI_SITEMAP_ENTRY_NO_TITLE_0";     
    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.editors.sitemap.messagesSitemap";


    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new MessagesSitemap();

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private MessagesSitemap() {

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
