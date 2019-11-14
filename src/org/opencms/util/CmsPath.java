/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.util;

/**
 * Simple data holder class which stores a path in a normalized form.<p>
 *
 * This is mostly useful when using paths as map keys, when you are not sure if the paths you are processing have a trailing slash
 * or not.<p>
 *
 * The paths are stored in the form '/foo/bar/baz', i.e. they include a leading but no trailing slash, except in the case of the root path '/'.
 */
public class CmsPath {

    /** The internal path string. */
    private String m_pathStr;

    /**
     * Creates a new instance.
     *
     * @param pathStr the path string
     */
    public CmsPath(String pathStr) {

        if (pathStr.equals("") || pathStr.equals("/")) {
            m_pathStr = "/";
        } else {
            m_pathStr = CmsFileUtil.removeTrailingSeparator(pathStr);
            if (!m_pathStr.startsWith("/")) {
                m_pathStr = "/" + m_pathStr;
            }
        }
    }

    /**
     * Gets the path as a string.
     *
     * @return the path as a string.
     */
    public String asString() {

        return m_pathStr;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsPath)) {
            return false;
        }
        return ((CmsPath)obj).m_pathStr.equals(m_pathStr);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_pathStr.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return super.toString() + "[" + asString() + "]";
    }

}
