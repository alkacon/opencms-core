
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/I_CmsLogChannels.java,v $
* Date   : $Date: 2001/05/10 12:30:03 $
* Version: $Revision: 1.4 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.boot;


/**
 * Common interface for OpenCms logging.
 * Constants used for logging purposes are defined here.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.4 $ $Date: 2001/05/10 12:30:03 $
 */
public interface I_CmsLogChannels {
    /** Debugging messages */
    public static final String C_OPENCMS_INIT = "opencms_init";

    /** Debugging messages */
    public static final String C_OPENCMS_DEBUG = "opencms_debug";

    /** Cache messages */
    public static final String C_OPENCMS_CACHE = "opencms_cache";

    /** Debugging messages for the streaming mode */
    public static final String C_OPENCMS_STREAMING = "opencms_streaming";

    /** Informational messages */
    public static final String C_OPENCMS_INFO = "opencms_info";

    /** Critical messages that stop further processing */
    public static final String C_OPENCMS_CRITICAL = "opencms_critical";

    /** Debugging messages for the dbpool */
    public static final String C_OPENCMS_POOL = "opencms_pool";

    /** Messages of the new OpenCms element cache */
    public static final String C_OPENCMS_ELEMENTCACHE = "opencms_elementcache";


    /** Debugging messages */
    public static final String C_MODULE_DEBUG = "module_debug";

    /** Informational messages */
    public static final String C_MODULE_INFO = "module_info";

    /** Critical messages that stop further processing */
    public static final String C_MODULE_CRITICAL = "module_critical";
}
