/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.shared;

import org.opencms.relations.CmsCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive category tree entry.<p>
 * 
 * @since 8.0.0
 */
public class CmsCategoryTreeEntry extends CmsCategoryBean {

    /** The children. */
    private List<CmsCategoryTreeEntry> m_children;

    /**
     * Clone constructor.<p>
     * 
     * @param category the category to clone
     * 
     * @throws Exception will never happen 
     */
    public CmsCategoryTreeEntry(CmsCategory category)
    throws Exception {

        super(category);
    }

    /**
     * Constructor for serialization.<p>
     */
    protected CmsCategoryTreeEntry() {

        // do nothing
    }

    /**
     * Adds a child entry.<p>
     * 
     * @param child the child to add
     */
    public void addChild(CmsCategoryTreeEntry child) {

        if (m_children == null) {
            m_children = new ArrayList<CmsCategoryTreeEntry>();
        }
        m_children.add(child);
    }

    /**
     * Returns the children.<p>
     *
     * @return the children
     */
    public List<CmsCategoryTreeEntry> getChildren() {

        return m_children;
    }

    /**
     * Sets the children.<p>
     *
     * @param children the children to set
     */
    public void setChildren(List<CmsCategoryTreeEntry> children) {

        m_children = children;
    }
}