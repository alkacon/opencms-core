/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/validation/Attic/Messages.java,v $
 * Date   : $Date: 2005/06/22 15:33:01 $
 * Version: $Revision: 1.7 $
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

package org.opencms.validation;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LINK_POINTING_TO_0 = "GUI_LINK_POINTING_TO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LINK_POINTING_TO_2 = "GUI_LINK_POINTING_TO_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LINK_VALIDATION_RESULTS_ALL_VALID_1 = "GUI_LINK_VALIDATION_RESULTS_ALL_VALID_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LINK_VALIDATION_RESULTS_INTRO_1 = "GUI_LINK_VALIDATION_RESULTS_INTRO_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LINK_SEARCH_1 = "LOG_LINK_SEARCH_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_RETRIEVAL_RESOURCE_1 = "LOG_RETRIEVAL_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_BROKEN_0 = "RPT_BROKEN_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_BROKEN_LINKS_IN_1 = "RPT_BROKEN_LINKS_IN_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_BROKEN_LINKS_SUMMARY_BEGIN_0 = "RPT_BROKEN_LINKS_SUMMARY_BEGIN_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_BROKEN_LINKS_SUMMARY_END_0 = "RPT_BROKEN_LINKS_SUMMARY_END_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_HTMLLINK_FOUND_BROKEN_LINKS_0 = "RPT_HTMLLINK_FOUND_BROKEN_LINKS_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_HTMLLINK_VALIDATING_0 = "RPT_HTMLLINK_VALIDATING_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_HTMLLINK_VALIDATOR_BEGIN_0 = "RPT_HTMLLINK_VALIDATOR_BEGIN_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_HTMLLINK_VALIDATOR_END_0 = "RPT_HTMLLINK_VALIDATOR_END_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_HTMLLINK_VALIDATOR_ERROR_0 = "RPT_HTMLLINK_VALIDATOR_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_LINK_VALIDATION_STAT_2 = "RPT_LINK_VALIDATION_STAT_2";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_VALIDATE_EXTERNAL_LINKS_BEGIN_0 = "RPT_VALIDATE_EXTERNAL_LINKS_BEGIN_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_VALIDATE_EXTERNAL_LINKS_END_0 = "RPT_VALIDATE_EXTERNAL_LINKS_END_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_VALIDATE_LINK_0 = "RPT_VALIDATE_LINK_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.validation.messages";

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
