/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/I_CmsFlexCacheEnabledLoader.java,v $
 * Date   : $Date: 2004/11/05 18:15:11 $
 * Version: $Revision: 1.1 $
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
 
package org.opencms.loader;

import org.opencms.flex.CmsFlexCache;

/**
 * Resource loaders that implement this interface are flex cache enabled.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.4
 */
public interface I_CmsFlexCacheEnabledLoader {

    /**
     * Will be called after the resource manager was initialized.<p>
     * 
     * @param cache the (optional) flex cache instance to use
     */
    void setFlexCache(CmsFlexCache cache);
}
