/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/I_CmsLogChannels.java,v $
* Date   : $Date: 2003/08/14 15:37:26 $
* Version: $Revision: 1.13 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.boot;


/**
 * Common interface for OpenCms logging,
 * constants used for logging purposes are defined here.
 *
 * @author Alexander Lucas
 * @author Andreas Schouten
 * 
 * @version $Revision: 1.13 $ $Date: 2003/08/14 15:37:26 $
 */
public interface I_CmsLogChannels {
    
    /** Debugging messages */
    static final String C_OPENCMS_INIT = "opencms_init";

    /** Debugging messages */
    static final String C_OPENCMS_DEBUG = "opencms_debug";

    /** Cache messages */
    static final String C_OPENCMS_CACHE = "opencms_cache";

    /** Debugging messages for the streaming mode */
    static final String C_OPENCMS_STREAMING = "opencms_streaming";

    /** Informational messages */
    static final String C_OPENCMS_INFO = "opencms_info";

    /** Critical messages that stop further processing */
    static final String C_OPENCMS_CRITICAL = "opencms_critical";

    /** Debugging messages for the dbpool */
    static final String C_OPENCMS_POOL = "opencms_pool";

    /** Messages of the new OpenCms element cache */
    static final String C_OPENCMS_ELEMENTCACHE = "opencms_elementcache";

    /** Messages of the OpenCms Scheduler */
    static final String C_OPENCMS_CRONSCHEDULER = "opencms_cronscheduler";

    /** Messages of the static export */
    static final String C_OPENCMS_STATICEXPORT = "opencms_staticexport";


    /** Debugging messages */
    static final String C_MODULE_DEBUG = "module_debug";

    /** Informational messages */
    static final String C_MODULE_INFO = "module_info";

    /** Critical messages that stop further processing */
    static final String C_MODULE_CRITICAL = "module_critical";
    
    
    /** Flex cache messages */
    static final String C_FLEX_CACHE = "flex_cache";
    
    /** Flex loader messages */
    static final String C_FLEX_LOADER = "flex_loader"; 
}
