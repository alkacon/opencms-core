/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.site.xmlsitemap;

import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A helper class used for calculating which paths need to be included or excluded from the XML sitemap generation.<p>
 */
public class CmsPathIncludeExcludeSet {

    /** The set of all paths of includes or excludes. */
    private Set<String> m_allPaths = new TreeSet<String>();

    /** The set of excluded paths. */
    private Set<String> m_excludes = new HashSet<String>();

    /** The set of included paths. */
    private Set<String> m_includes = new HashSet<String>();

    /**
     * Adds an excluded path.<p>
     *
     * @param exclude the path to add
     */
    public void addExclude(String exclude) {

        exclude = normalizePath(exclude);
        m_excludes.add(exclude);
        m_allPaths.add(exclude);
    }

    /**
     * Adds an included path.<p>
     *
     * @param include the path to add
     */
    public void addInclude(String include) {

        include = normalizePath(include);
        m_includes.add(include);
        m_allPaths.add(include);
    }

    /**
     * Gets the root include paths, i.e. those include paths which don't have an ancestor path which is also an include path.<p>
     *
     * @return the list of root include paths
     */
    public Set<String> getIncludeRoots() {

        List<String> pathList = new ArrayList<String>(m_includes);
        Set<String> includeRoots = new HashSet<String>();
        Collections.sort(pathList);
        while (!pathList.isEmpty()) {
            Iterator<String> iterator = pathList.iterator();
            String firstPath = iterator.next();
            includeRoots.add(firstPath);
            iterator.remove();
            while (iterator.hasNext()) {
                String currentPath = iterator.next();
                if (CmsStringUtil.isPrefixPath(firstPath, currentPath)) {
                    iterator.remove();
                }
            }
        }
        return includeRoots;
    }

    /**
     * Checks if the given path is excluded by the include/exclude configuration.<p>
     *
     * @param path the path to check
     *
     * @return true if the path is excluded
     */
    public boolean isExcluded(String path) {

        path = normalizePath(path);
        List<String> pathList = new ArrayList<String>(m_allPaths);
        // m_allPaths is already sorted, we need the reverse ordering so children come before their parents
        Collections.reverse(pathList);
        for (String pathInList : pathList) {
            if (CmsStringUtil.isPrefixPath(pathInList, path)) {
                return m_excludes.contains(pathInList);
            }
        }
        return false;
    }

    /**
     * Converts a path to a normalized form.<p>
     *
     * @param path the path to normalize
     *
     * @return the normalized path
     */
    protected String normalizePath(String path) {

        if (path.equals("/")) {
            return path;
        } else {
            return CmsFileUtil.removeTrailingSeparator(path);
        }

    }
}
