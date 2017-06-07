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

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure which stores contents indexed by path.
 *
 * @param <V> the element type of the path map
 */
public class CmsPathMap<V> {

    /** The tree used for storing the contents. */
    private CmsPathTree<String, V> m_tree = new CmsPathTree<String, V>();

    /**
     * Adds an element for the given path, overwriting the previous element for that path if there is one.<p>
     *
     * @param path the path
     * @param value the element to add
     */
    public void add(String path, V value) {

        m_tree.setValue(splitPath(path), value);
    }

    /**
     * Gets the values for the direct children of the given path.<p>
     *
     * @param path the path
     * @return the child values
     */
    public List<V> getChildValues(String path) {

        return m_tree.getChildValues(splitPath(path));
    }

    /**
     * Gets the values for the descendants of the path, including the path itself.<p>
     *
     * @param path the path
     * @return the descendant values for the path
     */
    public List<V> getDescendantValues(String path) {

        return m_tree.getDescendantValues(splitPath(path));
    }

    /**
     * Converts a path into list form.<p>
     *
     * @param path the path to convert
     * @return the list of the path elements
     */
    private List<String> splitPath(String path) {

        List<String> result = new ArrayList<String>();
        for (String token : path.split("/")) {
            if ("".equals(token)) {
                continue;
            }
            result.add(token);
        }
        return result;
    }

}
