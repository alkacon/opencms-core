/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/Messages.java,v $
 * Date   : $Date: 2005/06/22 10:38:16 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package com.opencms.legacy;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Thomas Weckert  
 * @author Jan Baudisch 
 * @since 5.7.3
 */
public final class Messages extends A_CmsMessageBundle {
   
    /** Message constant for key in the resource bundle. */
    public static final String ERR_COS_ACCESS_CHANNEL_1 = "ERR_COS_ACCESS_CHANNEL_1";
    
    /** Message constant for key in the resource bundle. */
    public static final String ERR_COS_IMPORTEXPORT_ERROR_ADDING_CHILD_RESOURCES_1 = "ERR_COS_IMPORTEXPORT_ERROR_ADDING_CHILD_RESOURCES_1";
   
    /** Message constant for key in the resource bundle. */
    public static final String ERR_COS_IMPORTEXPORT_ERROR_EXPORTING_TO_FILE_1 = "ERR_COS_IMPORTEXPORT_ERROR_EXPORTING_TO_FILE_1";
    
    /** Message constant for key in the resource bundle. */
    public static final String ERR_COS_IMPORTEXPORT_ERROR_IMPORTING_RESOURCES_0 = "ERR_COS_IMPORTEXPORT_ERROR_IMPORTING_RESOURCES_0";
   
    /** Message constant for key in the resource bundle. */
    public static final String ERR_COS_INDEXING_CONTENTS_1 = "ERR_COS_INDEXING_CONTENTS_1";
   
    /** Message constant for key in the resource bundle. */
    public static final String ERR_COS_INDEXING_CONTENTS_OF_CLASS_1 = "ERR_COS_INDEXING_CONTENTS_OF_CLASS_1";
   
    /** Message constant for key in the resource bundle. */
    public static final String RPT_ARGUMENT_2 = "RPT_ARGUMENT_2";
   
    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXPORT_CHANNELS_BEGIN_0 = "RPT_EXPORT_CHANNELS_BEGIN_0";
   
    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXPORT_CHANNELS_END_0 = "RPT_EXPORT_CHANNELS_END_0";
   
    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXPORT_MODULE_BEGIN_1 = "RPT_EXPORT_MODULE_BEGIN_1";
   
    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXPORT_MODULE_END_0 = "RPT_EXPORT_MODULE_END_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXPORT_WITH_ID_2 = "RPT_EXPORT_WITH_ID_2";
    
    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMPORT_CHANNELS_BEGIN_0 = "RPT_IMPORT_CHANNELS_BEGIN_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMPORT_CHANNELS_END_0 = "RPT_IMPORT_CHANNELS_END_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMPORT_MODULE_BEGIN_0 = "RPT_IMPORT_MODULE_BEGIN_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMPORT_MODULE_END_0 = "RPT_IMPORT_MODULE_END_0";
   
    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMPORT_VERSION_1 = "RPT_IMPORT_VERSION_1";
   
    /** Message constant for key in the resource bundle. */
    public static final String RPT_INDEX_CHANNEL_0 = "RPT_INDEX_CHANNEL_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String RPT_MUST_SET_TO_1 = "RPT_MUST_SET_TO_1";
    
    /** Message constant for key in the resource bundle. */
    public static final String RPT_NONEXISTENT_PUBLISH_CLASS_FOR_MODULE_1 = "RPT_NONEXISTENT_PUBLISH_CLASS_FOR_MODULE_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "com.opencms.legacy";

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
