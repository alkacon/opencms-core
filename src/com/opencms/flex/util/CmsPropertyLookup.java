/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/util/Attic/CmsPropertyLookup.java,v $
 * Date   : $Date: 2002/09/19 12:26:12 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
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
 *
 * First created on 3.September 2002
 */

package com.opencms.flex.util;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.flex.cache.CmsFlexRequest;

/**
 * This class does a "cascaded" propery lookup.
 * It looks up a property on the selected file,
 * and if it does not find the property there,
 * it continues to look up the property at the folder of the 
 * file, then on the parent folder of the folder etc. until
 * it reached the root folder.<p>
 * 
 * TODO: It also caches it's results to ensure that this expensive operation 
 * is not repeated to often.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.5 $
 */
public class CmsPropertyLookup {
    
    /**
     * Look up a specified property with optional direcory upward cascading.
     * 
     * @param cms The current CmsObject
     * @param resource The resource to look up the property for
     * @param property The name of the property to look up
     * @param search If true, the property will be looked up on all parent folders
     *   if it is not attached to the the resource, if false not (ie. normal 
     *   property lookup)
     * @return The value of the property found, null if nothing was found
     * @throws CmsException In case there were problems reading the property on the CmsObject
     */
    public static String lookupProperty(CmsObject cms, String resource, String property, boolean search)
    throws CmsException {
        if (search) {
            String f = resource;
            String prop = cms.readProperty(f, property);
            while ((prop == null) && (! "".equals(f))) {
                f = f.substring(0, f.lastIndexOf("/"));
                prop = cms.readProperty(f + "/", property);
            }      
            return prop;
        } else {
            return cms.readProperty(resource, property);
        }        
    }

    /**
     * Look up a specified property with optional direcory upward cascading.
     * A default value will be returned if the property is not found on the
     * resource (or it's parent folders in case search is set to "true").
     * 
     * @param cms The current CmsObject
     * @param resource The resource to look up the property for
     * @param property The name of the property to look up
     * @param search If true, the property will be looked up on all parent folders
     *   if it is not attached to the the resource, if false not (ie. normal 
     *   property lookup)
     * @param propertyDefault A default value that will be returned if
     *   the property was not found at the selected resource
     * @return The value of the property found, null if nothing was found
     * @throws CmsException In case there were problems reading the property on the CmsObject
     */
    public static String lookupProperty(CmsObject cms, String resource, String property, boolean search, String propertyDefault)
    throws CmsException {
        String prop = lookupProperty(cms, resource, property, search);
        if ((prop == null) || "".equals(prop)) {
            prop = propertyDefault;
        }
        return prop;
    }

}
