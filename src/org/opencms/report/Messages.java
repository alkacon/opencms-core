/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/Messages.java,v $
 * Date   : $Date: 2005/06/23 11:11:28 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.report;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String RPT_ARGUMENT_1 = "RPT_ARGUMENT_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_ARGUMENT_HTML_ITAG_1 = "RPT_ARGUMENT_HTML_ITAG_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_DOTS_0 = "RPT_DOTS_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXCEPTION_0 = "RPT_EXCEPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_FAILED_0 = "RPT_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_IGNORED_0 = "RPT_IGNORED_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_OK_0 = "RPT_OK_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_SKIPPED_0 = "RPT_SKIPPED_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_STAT_0 = "RPT_STAT_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_STAT_DURATION_1 = "RPT_STAT_DURATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_SUCCESSION_1 = "RPT_SUCCESSION_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_SUCCESSION_2 = "RPT_SUCCESSION_2";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.report.messages";

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
