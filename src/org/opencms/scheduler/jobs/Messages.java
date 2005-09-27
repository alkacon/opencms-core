/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/jobs/Messages.java,v $
 * Date   : $Date: 2005/09/27 12:15:56 $
 * Version: $Revision: 1.6.2.1 $
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

package org.opencms.scheduler.jobs;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.6.2.1 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String LOG_PUBLISH_FAILED_2 = "LOG_PUBLISH_FAILED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_PUBLISH_FINISHED_1 = "LOG_PUBLISH_FINISHED_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMAGE_SIZE_END_0 = "RPT_IMAGE_SIZE_END_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMAGE_SIZE_LOCKED_0 = "RPT_IMAGE_SIZE_LOCKED_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMAGE_SIZE_PROCESS_3 = "RPT_IMAGE_SIZE_PROCESS_3";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMAGE_SIZE_SKIP_1 = "RPT_IMAGE_SIZE_SKIP_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMAGE_SIZE_START_0 = "RPT_IMAGE_SIZE_START_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMAGE_SIZE_UPDATE_1 = "RPT_IMAGE_SIZE_UPDATE_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.scheduler.jobs.messages";

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
