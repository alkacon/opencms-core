/*
 * File   : $Source $
 * Date   : $Date $
 * Version: $Revision $
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
 * It also caches it's results to ensure that this expensive operation 
 * is not repeated to often.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision $
 */
public class CmsPropertyLookup {
    

    public static String lookupProperty(CmsObject cms, String fileName, String propertyName, boolean search)
    throws CmsException {
        if (search) {
            String f = fileName;
            String prop = cms.readProperty(f, propertyName);
            while ((prop == null) && (! "".equals(f))) {
                f = f.substring(0, f.lastIndexOf("/"));
                prop = cms.readProperty(f + "/", propertyName);
            }      
            return prop;
        } else {
            return cms.readProperty(fileName, propertyName);
        }        
    }


}
