/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/Messages.java,v $
 * Date   : $Date: 2005/04/26 12:50:49 $
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

package org.opencms.threads;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch (j.baudisch@alkacon.com)
 * @since 5.7.3
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message contant for key in the resource bundle. */
    public static final String ERR_DB_EXPORT_0 = "ERR_DB_EXPORT_0";
    
    /** Message contant for key in the resource bundle. */
    public static final String ERR_LINK_VALIDATION_0 = "ERR_LINK_VALIDATION_0";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_DELETE_THREAD_CONSTRUCTED_0 = "LOG_DELETE_THREAD_CONSTRUCTED_0";    
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_DELETE_THREAD_FINISHED_0 = "LOG_DELETE_THREAD_FINISHED_0";    
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_DELETE_THREAD_STARTED_0 = "LOG_DELETE_THREAD_STARTED_0";
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_MODULE_DELETE_FAILED_1 = "LOG_MODULE_DELETE_FAILED_1";
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_MOVE_RESOURCE_FAILED_1 = "LOG_MOVE_RESOURCE_FAILED_1";
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_PROJECT_DELETE_FAILED_1 = "LOG_PROJECT_DELETE_FAILED_1";
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_PUBLISH_PROJECT_FAILED_0 = "LOG_PUBLISH_PROJECT_FAILED_0";
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_REPLACE_THREAD_CONSTRUCTED_0 = "LOG_REPLACE_THREAD_CONSTRUCTED_0";
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_REPLACE_THREAD_FINISHED_0 = "LOG_REPLACE_THREAD_FINISHED_0";
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_REPLACE_THREAD_START_DELETE_0 = "LOG_REPLACE_THREAD_START_DELETE_0";
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_REPLACE_THREAD_START_IMPORT_0 = "LOG_REPLACE_THREAD_START_IMPORT_0";
 
    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.threads.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Returns an instance of this localized message accessor.<p>
     * 
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
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
