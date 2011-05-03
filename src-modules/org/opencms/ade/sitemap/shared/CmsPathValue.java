/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsPathValue.java,v $
 * Date   : $Date: 2011/05/03 10:49:06 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.shared;

import org.opencms.util.CmsStringUtil;

/**
 * A bean which represents a value together with a path which indicates from where the value has been read.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsPathValue {

    /** The path. */
    private final String m_path;

    /** The value. */
    private final String m_value;

    /**
     * Creates a new instance.<p> 
     * @param value the value 
     * @param path the path 
     */
    public CmsPathValue(String value, String path) {

        m_path = path;
        m_value = value;
    }

    /**
     * Gets the path.<p> 
     * 
     * @return the path 
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Gets the value.<p>
     * 
     * @return the value 
     */
    public String getValue() {

        return m_value;
    }

    /**
     * Creates a new path value with the same value as this one, but with a prefix prepended to the path.<p>
     * 
     * @param pathPart the path part which should be prepended to the path
     *  
     * @return the new path value 
     */
    public CmsPathValue prepend(String pathPart) {

        return new CmsPathValue(m_value, CmsStringUtil.joinPaths(pathPart, m_path));
    }

}
