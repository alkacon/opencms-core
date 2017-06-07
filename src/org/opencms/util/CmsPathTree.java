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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Tree used to represent file system like data structures.<p>
 *
 * A tree consists of a (possibly empty) value and a map from child names to subtrees.<p>
 *
 * @param <P> the type of path components
 * @param <V> the element type stored in the tree
 *
 */
public class CmsPathTree<P, V> {

    /** The map from child names to children. */
    private Map<P, CmsPathTree<P, V>> m_children = Maps.newHashMap();

    /** The value (may be null). */
    private V m_value;

    /**
     * Collect all descendant  values in the given collection.<p>
     *
     * @param target the collection in which to store the descendant values
     */
    public void collectEntries(Collection<V> target) {

        if (m_value != null) {
            target.add(m_value);
        }
        for (CmsPathTree<P, V> child : m_children.values()) {
            child.collectEntries(target);
        }
    }

    /**
     * Finds the node for the given path, and returns it or null if node was found.<p>
     *
     * @param path the path
     * @return the node for the path
     */
    public CmsPathTree<P, V> findNode(List<P> path) {

        List<P> pathToConsume = Lists.newLinkedList(path);
        CmsPathTree<P, V> descendant = findNodeInternal(pathToConsume);
        if (!pathToConsume.isEmpty()) {
            return null;
        } else {
            return descendant;
        }
    }

    /**
     * Gets the child for the given path component.<p>
     *
     * @param pathPart the path component
     *
     * @return the child for the given path component (may be null)
     */
    public CmsPathTree<P, V> getChild(P pathPart) {

        return m_children.get(pathPart);
    }

    /**
     * Returns the values for the direct children of this node.<p>
     *
     * @return the values for the direct children
     */
    public List<V> getChildValues() {

        List<V> result = Lists.newArrayList();
        for (CmsPathTree<P, V> child : m_children.values()) {
            if (child.m_value != null) {
                result.add(child.m_value);
            }
        }
        return result;
    }

    /**
     * Gets the child values for the given path.<p>
     *
     * @param path the path
     *
     * @return the child values
     */
    public List<V> getChildValues(List<P> path) {

        CmsPathTree<P, V> descendant = findNode(path);
        if (descendant != null) {
            return descendant.getChildValues();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Gets the descendant values.<p>
     *
     * @param path the path
     * @return the descendant values
     */
    public List<V> getDescendantValues(List<P> path) {

        CmsPathTree<P, V> node = findNode(path);
        List<V> result = Lists.newArrayList();
        if (node != null) {
            node.collectEntries(result);
        }
        return result;
    }

    /**
     * Gets the value for this node (may be null).<p>
     *
     * @return the value
     */
    public V getValue() {

        return m_value;
    }

    /**
     * Gets the value for the sub-path given, starting from this node.<p>
     *
     * @param path the path
     * @return the value for the node
     */
    public V getValue(List<P> path) {

        CmsPathTree<P, V> node = findNode(path);
        if (node != null) {
            return node.m_value;
        } else {
            return null;
        }
    }

    /**
     * Sets the value for the sub-path given, starting from this node.<p>
     *
     * @param path the  path
     * @param value the value to set
     */
    public void setValue(List<P> path, V value) {

        ensureNode(path).setValue(value);

    }

    /**
     * Sets the value for this node.<p>
     *
     * @param value the value for the node
     */
    public void setValue(V value) {

        m_value = value;
    }

    /**
     * Returns the node for the given path, creating all nodes on the way if they don't already exist.<p>
     *
     * @param path the path for which to make sure a node exists
     *
     * @return the node for the path
     */
    private CmsPathTree<P, V> ensureNode(List<P> path) {

        List<P> pathToConsume = Lists.newLinkedList(path);
        CmsPathTree<P, V> lastExistingNode = findNodeInternal(pathToConsume);
        CmsPathTree<P, V> currentNode = lastExistingNode;
        for (P pathPart : pathToConsume) {
            CmsPathTree<P, V> child = new CmsPathTree<P, V>();
            currentNode.m_children.put(pathPart, child);
            currentNode = child;
        }
        return currentNode;
    }

    /**
     * Tries to traverse the descendants of this node along the given path,
     * and returns the last existing node along that path.<p>
     *
     * The given path is modified so that only the part of the path for which no nodes can be found
     * remains.<p>
     *
     * @param pathToConsume the path to find (will be modified by this method)
     * @return the last node found along the descendant chain for the path
     */
    private CmsPathTree<P, V> findNodeInternal(List<P> pathToConsume) {

        Iterator<P> iter = pathToConsume.iterator();
        CmsPathTree<P, V> currentNode = this;
        while (iter.hasNext()) {
            CmsPathTree<P, V> child = currentNode.getChild(iter.next());
            if (child != null) {
                iter.remove();
                currentNode = child;
            } else {
                return currentNode;
            }
        }
        return currentNode;
    }

}
