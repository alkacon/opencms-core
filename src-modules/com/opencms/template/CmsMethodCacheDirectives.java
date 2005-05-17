/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/template/Attic/CmsMethodCacheDirectives.java,v $
* Date   : $Date: 2005/05/17 13:47:32 $
* Version: $Revision: 1.1 $
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


package com.opencms.template;

import com.opencms.template.cache.CmsTimeout;

/**
 * Collection of all information about cacheability and
 * used keys for the Methodelements.
 *
 * @author Hanjo Riege
 * @version 1.0
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsMethodCacheDirectives extends A_CmsCacheDirectives {


    /**
     * Constructor.<p>
     * 
     * @param internal Initial value for "internal cacheable" property.
     */
    public CmsMethodCacheDirectives(boolean internal) {
        setExternalCaching(internal, true, true, true, true);
    }

    /**
     * set the timeout object(used if the element should be reloaded every x minutes.
     * @param timeout a CmsTimeout object.
     */
    public void setTimeout(CmsTimeout timeout) {
        m_timecheck = true;
        m_timeout = timeout;
    }


}