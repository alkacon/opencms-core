/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsClosestPathFinder.java,v $
 * Date   : $Date: 2010/07/20 11:50:24 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.sitemap;

import org.opencms.util.CmsPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class for finding the path among a list of paths which is "closest"
 * to a given path.<p>
 * 
 * Closest here is defined as sharing a maximal number of initial path components with the path.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsClosestPathFinder {

    /** The list of paths from to which paths will be compared. */
    private Map<String, String[]> m_otherPaths = new HashMap<String, String[]>();

    /**
     * Adds a new path.<p>
     * 
     * @param path the path to add 
     */
    public void addPath(String path) {

        String[] pathTokens = getPathComponents(path);
        m_otherPaths.put(path, pathTokens);
    }

    /**
     * Adds several new paths.<p>
     * 
     * @param paths the paths to add 
     */
    public void addPaths(Collection<String> paths) {

        for (String path : paths) {
            addPath(path);
        }
    }

    /**
     * Gets the path closest to <code>path</code> among the paths which have been added via {@link #addPath(String)}.<p>
     * 
     * @param path the path to compare to the other paths 
     * @return the closest path to <code>path</code>
     */
    public String getClosestPath(String path) {

        assert m_otherPaths.size() > 0;
        String[] pathTokens = getPathComponents(path);
        List<CmsPair<Integer, String>> countsAndPaths = new ArrayList<CmsPair<Integer, String>>();

        //Collect a list of pairs containing each a path together with its count of components shared 
        //with the path passed as a parameter   
        for (Map.Entry<String, String[]> entry : m_otherPaths.entrySet()) {
            String origPath = entry.getKey();
            String[] origPathTokens = entry.getValue();
            int sharedComponentCount = countSharedInitialComponents(pathTokens, origPathTokens);
            CmsPair<Integer, String> countAndPath = new CmsPair<Integer, String>(
                new Integer(sharedComponentCount),
                origPath);
            countsAndPaths.add(countAndPath);
        }
        //Pick the pair which has a maximal first component
        CmsPair<Integer, String> maxPair = Collections.max(
            countsAndPaths,
            CmsPair.<Integer, String> getLexicalComparator());
        return maxPair.getSecond();
    }

    /**
     * Counts the shared initial components of two paths given as string arrays.<p>
     * 
     * @param path1 the first path 
     * @param path2 the second path 
     * 
     * @return the number of shared initial components between the two paths  
     */
    protected int countSharedInitialComponents(String[] path1, String[] path2) {

        int size = Math.min(path1.length, path2.length);
        int count;
        for (count = 0; count < size; count++) {
            if (!path1[count].equals(path2[count])) {
                break;
            }
        }
        return count;
    }

    /**
     * Returns an array containing the components of a path.<p>
     * 
     * @param path the path to split into components  
     * 
     * @return the path components 
     */
    protected String[] getPathComponents(String path) {

        path = stripLeadingAndTrailingSlashes(path);
        return path.split("/");
    }

    /**
     * Strips leading and trailing slashes from a path.<p>
     * 
     * @param path the path from which the slashes should be stripped 
     * 
     * @return the path with leading and trailing slashes removed 
     */
    protected String stripLeadingAndTrailingSlashes(String path) {

        return path.replaceAll("(^/+|/+$)", "");
    }
}
