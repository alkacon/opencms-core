/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsAdjacencyTree.java,v $
 * Date   : $Date: 2003/07/03 13:29:45 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db;

import com.opencms.file.CmsResource;
import com.opencms.flex.util.CmsUUID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A tree represented by the adjacency list model.<p>
 * 
 * The tree is built in-memory using a Map keyed by CmsUUID parent ID's with values that are 
 * ArrayLists of child CmsResources.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2003/07/03 13:29:45 $
 * @since 5.1.3
 */
public class CmsAdjacencyTree extends Object implements Serializable, Cloneable {

    /** The number of resources in the tree */
    private int m_size;

    /** The map representing the adjacency list tree */
    private Map m_treeMap;

    /**
     * Constructs an empty tree.<p>
     */
    public CmsAdjacencyTree() {
        m_treeMap = (Map) new HashMap();
        m_size = 0;
    }

    /**
     * Adds a CmsResource to the tree.<p>
     * 
     * @param resource the CmsResource to add
     */
    public void addResource(CmsResource resource) {
        // get the child list of the current folder
        List children = (List) m_treeMap.get(resource.getParentId());

        if (children == null) {
            // the current folder is obviously the first child of it's parent folder
            children = (List) new ArrayList();

            // add the new child list as a sub-tree
            m_treeMap.put(resource.getParentId(), children);
        }

        // add the current folder to the child list
        children.add(resource);

        // sort the children by their resource name        
        Collections.sort(children);

        // increment the size counter
        m_size++;
    }

    /**
     * Removes all resources from the tree.<p>
     */
    public void clear() {
        if (m_treeMap == null) {
            return;
        }

        // iterate over all adjacency lists to clear them
        Iterator i = m_treeMap.keySet().iterator();
        while (i.hasNext()) {
            CmsUUID currentParentId = (CmsUUID) i.next();

            // clear and remove the current adjacency list
            List currentAdjacencyList = (List) m_treeMap.get(currentParentId);
            currentAdjacencyList.clear();
            m_treeMap.remove(currentParentId);
        }

        // clear the tree map itself
        m_treeMap.clear();
        m_size = 0;
    }

    /**
     * Releases any allocated resources during garbage collection.<p>
     * 
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        clear();
        m_treeMap = null;
    }

    /**
     * Recursively builds a DFS list view of a sub-tree including the parent resource.<p>
     * 
     * @param parentResource the CmsResource from where the sub-tree is built
     * @return a DFS list view of the sub-tree with sub-resources in alphabetical order
     */
    public List toList(CmsResource parentResource) {
        if (parentResource == null) {
            return null;
        }

        List result = (List) new ArrayList();
        result.add(parentResource);

        // get the adjacency list with the child resources of the current parent resource
        List children = (List) m_treeMap.get(parentResource.getId());
        if (children == null) {
            return result;
        }

        // add the sub-tree of the current parent resource to the result list
        Iterator i = children.iterator();
        while (i.hasNext()) {
            CmsResource currentChild = (CmsResource) i.next();
            result.addAll(toList(currentChild));
        }

        return result;
    }

    /**
     * Returns true if this tree is empty.<p>
     * 
     * @return true if this tree is empty
     */
    public boolean isEmpty() {
        return m_size == 0;
    }

    /**
     * Returns the number of resources in the tree.<p>
     * 
     * @return the number of resources in the tree
     */
    public int size() {
        return m_size;
    }
    
}
