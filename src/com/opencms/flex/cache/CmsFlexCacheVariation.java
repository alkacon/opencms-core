/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/cache/Attic/CmsFlexCacheVariation.java,v $
 * Date   : $Date: 2002/09/04 15:43:55 $
 * Version: $Revision: 1.1 $
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
 */

package com.opencms.flex.cache;

import java.util.*;

/**
 * @author Thomas Weckert, <a href="mailto:t.weckert@alkacon.com">t.weckert@alkacon.com</a>; Alexander Kandzior, <a href="mailto:a.kandzior@alkacon.com">a.kandzior@alkacon.com</a>
 */
public class CmsFlexCacheVariation extends Object {
    
    /** The key belonging to the resource */
    public CmsFlexCacheKey key;
    
    /** Maps variations to CmsFlexCacheEntries */
    public Map map;
    
    /**
     * Generates a new instance of CmsFlexCacheVariation
     *
     * @param theKey The (resource) key to contruct this variation list for
     */
    public CmsFlexCacheVariation(CmsFlexCacheKey theKey ) {
        this.key = theKey;
        this.map = java.util.Collections.synchronizedMap( new HashMap(CmsFlexCache.C_INITIAL_CAPACITY_VARIATIONS) );
    }
}
