/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/I_CmsLogChannels.java,v $
 * Date   : $Date: 2003/09/02 12:15:38 $
 * Version: $Revision: 1.14 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 
package com.opencms.boot;

/**
 * Common interface for OpenCms logging,
 * constants used for logging purposes are defined here.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.14 $ 
 */
public interface I_CmsLogChannels {

    /** Flex cache messages */
    String C_FLEX_CACHE = "flex_cache";

    /** Flex loader messages */
    String C_FLEX_LOADER = "flex_loader";

    /** Critical messages that stop further processing */
    String C_MODULE_CRITICAL = "module_critical";

    /** Debugging messages */
    String C_MODULE_DEBUG = "module_debug";

    /** Informational messages */
    String C_MODULE_INFO = "module_info";

    /** Cache messages */
    String C_OPENCMS_CACHE = "opencms_cache";

    /** Critical messages that stop further processing */
    String C_OPENCMS_CRITICAL = "opencms_critical";

    /** Messages of the OpenCms Scheduler */
    String C_OPENCMS_CRONSCHEDULER = "opencms_cronscheduler";

    /** Debugging messages */
    String C_OPENCMS_DEBUG = "opencms_debug";

    /** Messages of the new OpenCms element cache */
    String C_OPENCMS_ELEMENTCACHE = "opencms_elementcache";

    /** Informational messages */
    String C_OPENCMS_INFO = "opencms_info";

    /** Debugging messages */
    String C_OPENCMS_INIT = "opencms_init";

    /** Debugging messages for the dbpool */
    String C_OPENCMS_POOL = "opencms_pool";

    /** Messages of the static export */
    String C_OPENCMS_STATICEXPORT = "opencms_staticexport";

    /** Debugging messages for the streaming mode */
    String C_OPENCMS_STREAMING = "opencms_streaming";
}
