/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/search/Messages.java,v $
 * Date   : $Date: 2011/03/23 14:52:48 $
 * Version: $Revision: 1.9 $
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 * All rights reserved.
 * 
 * This source code is the intellectual property of Alkacon Software GmbH.
 * It is PROPRIETARY and CONFIDENTIAL.
 * Use of this source code is subject to license terms.
 *
 * In order to use this source code, you need written permission from 
 * Alkacon Software GmbH. Redistribution of this source code, in modified 
 * or unmodified form, is not allowed unless written permission by 
 * Alkacon Software GmbH has been given.
 *
 * ALKACON SOFTWARE GMBH MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOURCE CODE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ALKACON SOFTWARE GMBH SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOURCE CODE OR ITS DERIVATIVES.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 */

package org.opencms.workplace.search;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.2.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INDEX_INVALID_1 = "ERR_INDEX_INVALID_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_VALIDATE_SEARCH_PARAMS_0 = "ERR_VALIDATE_SEARCH_PARAMS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_VALIDATE_SEARCH_QUERY_0 = "ERR_VALIDATE_SEARCH_QUERY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_DETAIL_EXCERPT_NAME_0 = "GUI_SEARCH_DETAIL_EXCERPT_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_DETAIL_HIDE_EXCERPT_HELP_0 = "GUI_SEARCH_DETAIL_HIDE_EXCERPT_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_DETAIL_HIDE_EXCERPT_NAME_0 = "GUI_SEARCH_DETAIL_HIDE_EXCERPT_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_DETAIL_SHOW_EXCERPT_HELP_0 = "GUI_SEARCH_DETAIL_SHOW_EXCERPT_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_DETAIL_SHOW_EXCERPT_NAME_0 = "GUI_SEARCH_DETAIL_SHOW_EXCERPT_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_EXPLORER_RESULTS_TOOL_GROUP_0 = "GUI_SEARCH_EXPLORER_RESULTS_TOOL_GROUP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_EXPLORER_RESULTS_TOOL_HELP_0 = "GUI_SEARCH_EXPLORER_RESULTS_TOOL_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_EXPLORER_RESULTS_TOOL_NAME_0 = "GUI_SEARCH_EXPLORER_RESULTS_TOOL_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_EXPLORER_RESULTS_TOOL_NAVNAME_0 = "GUI_SEARCH_EXPLORER_RESULTS_TOOL_NAVNAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_EXPLORER_TOOL_GROUP_0 = "GUI_SEARCH_EXPLORER_TOOL_GROUP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_EXPLORER_TOOL_HELP_0 = "GUI_SEARCH_EXPLORER_TOOL_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_EXPLORER_TOOL_NAME_0 = "GUI_SEARCH_EXPLORER_TOOL_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_FIELDS_TITLE_0 = "GUI_SEARCH_FIELDS_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_LIST_COLS_SCORE_0 = "GUI_SEARCH_LIST_COLS_SCORE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_LIST_COLS_SCORE_HELP_0 = "GUI_SEARCH_LIST_COLS_SCORE_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_LIST_NAME_0 = "GUI_SEARCH_LIST_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_QUERY_TITLE_0 = "GUI_SEARCH_QUERY_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEARCH_TIME_RANGES_0 = "GUI_SEARCH_TIME_RANGES_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.search.messages";

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
