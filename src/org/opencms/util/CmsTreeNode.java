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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Generic tree node which can contain an object of generic type T as data, and which has a mutable list
 * of child nodes.
 *
 * @param <T> the type of data associated with the tree node
 */
public class CmsTreeNode<T> {

    /** The list of child nodes. */
    private List<CmsTreeNode<T>> m_children = Lists.newArrayList();

    /** The data associated with this node. */
    private T m_data;

    /**
     * Adds a child node.<p>
     *
     * @param cmsTreeNode the child node to add
     */
    public void addChild(CmsTreeNode<T> cmsTreeNode) {

        m_children.add(cmsTreeNode);
    }

    /**
     * Gets the (mutable) list of child nodes.<p>
     *
     * @return the list of child nodes
     */
    public List<CmsTreeNode<T>> getChildren() {

        return m_children;
    }

    /**
     * Gets the data associated with this node.<p>
     *
     * @return the data for this node
     */
    public T getData() {

        return m_data;
    }

    /**
     * Sets the data for this node.<p>
     *
     * @param data the data to set
     */
    public void setData(T data) {

        m_data = data;
    }

}
