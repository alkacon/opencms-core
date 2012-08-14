/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.search.solr;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INDEX_SOLR_CONFIGURATION_MISS_1 = "ERR_INDEX_SOLR_CONFIGURATION_MISS_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INDEX_SOLR_EMBEDDED_START_2 = "ERR_INDEX_SOLR_EMBEDDED_START_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SEARCH_INVALID_SEARCH_1 = "ERR_SEARCH_INVALID_SEARCH_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SEARCH_INVALID_SEARCH_NO_PARAMS_0 = "ERR_SEARCH_INVALID_SEARCH_NO_PARAMS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SOLR_BAD_SERVER_URL_1 = "ERR_SOLR_BAD_SERVER_URL_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SOLR_CONFIG_XML_NOT_FOUND_1 = "ERR_SOLR_CONFIG_XML_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SOLR_CONFIG_XML_NOT_READABLE_1 = "ERR_SOLR_CONFIG_XML_NOT_READABLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SOLR_SCHEMA_XML_NOT_FOUND_1 = "ERR_SOLR_SCHEMA_XML_NOT_FOUND_1";

    /** Name of the used resource bundle. */
    public static final String LOG_INDEX_SOLR_EMBEDDED_CREATED_1 = "LOG_INDEX_SOLR_EMBEDDED_CREATED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INDEX_SOLR_INDEX_DIR_CREATED_2 = "LOG_INDEX_SOLR_INDEX_DIR_CREATED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INDEX_SOLR_CORE_CREATE_2 = "LOG_INDEX_SOLR_CORE_CREATE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_RESULT_ITERATION_FAILED_0 = "LOG_RESULT_ITERATION_FAILED_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.search.solr.messages";

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
