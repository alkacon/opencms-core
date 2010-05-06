/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/Messages.java,v $
 * Date   : $Date: 2010/05/06 09:27:20 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.gwt.client.i18n.CmsMessages;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
public final class Messages {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TAB_TITLE_GALLERIES_0 = "GUI_TAB_TITLE_GALLERIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TAB_TITLE_CATEGORIES_0 = "GUI_TAB_TITLE_CATEGORIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TAB_TITLE_SEARCH_0 = "GUI_TAB_TITLE_SEARCH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TAB_TITLE_TYPES_0 = "GUI_TAB_TITLE_TYPES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TAB_TITLE_RESULTS_0 = "GUI_TAB_TITLE_RESULTS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PARAMS_LABEL_GALLERY_0 = "GUI_PARAMS_LABEL_GALLERY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PARAMS_LABEL_GALLERIES_0 = "GUI_PARAMS_LABEL_GALLERIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PARAMS_LABEL_TYPE_0 = "GUI_PARAMS_LABEL_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PARAMS_LABEL_TYPES_0 = "GUI_PARAMS_LABEL_TYPES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PARAMS_LABEL_CATEGORY_0 = "GUI_PARAMS_LABEL_CATEGORY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PARAMS_LABEL_CATEGORIES_0 = "GUI_PARAMS_LABEL_CATEGORIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_TITLE_ASC_0 = "GUI_SORT_LABEL_TITLE_ASC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_TITLE_DECS_0 = "GUI_SORT_LABEL_TITLE_DECS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_TYPE_ASC_0 = "GUI_SORT_LABEL_TYPE_ASC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_TYPE_DESC_0 = "GUI_SORT_LABEL_TYPE_DESC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_HIERARCHIC_0 = "GUI_SORT_LABEL_HIERARCHIC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_DATELASTMODIFIED_ASC_0 = "GUI_SORT_LABEL_DATELASTMODIFIED_ASC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_DATELASTMODIFIED_DESC_0 = "GUI_SORT_LABEL_DATELASTMODIFIED_DESC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_PATH_ASC_0 = "GUI_SORT_LABEL_PATH_ASC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_PATH_DESC_0 = "GUI_SORT_LABEL_PATH_DESC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SORT_LABEL_SORT_0 = "GUI_SORT_LABEL_SORT_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.galleries.clientmessages";

    /** Static instance member. */
    private static CmsMessages INSTANCE;

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
    public static CmsMessages get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsMessages(BUNDLE_NAME);
        }
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
