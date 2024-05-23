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

package org.opencms.gwt.shared;

import org.opencms.relations.CmsCategory;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Recursive category tree entry.<p>
 *
 * @since 8.0.0
 */
public class CmsCategoryTreeEntry extends CmsCategoryBean {

    /** The children. */
    private List<CmsCategoryTreeEntry> m_children = Lists.newArrayList();

    /** 'Forced visible' state. */
    private Boolean m_forcedVisible;

    /**
     * Clone constructor.<p>
     *
     * @param category the category to clone
     */
    public CmsCategoryTreeEntry(CmsCategory category) {

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
     * Gets the 'forced visible' status.
     *
     * <p>A category tree entry with this status set to 'true' should be shown even if it is marked as hidden.
     *
     * @return the 'forced visible' status
     */
    public Boolean getForcedVisible() {

        return m_forcedVisible;
    }

    /**
     * Gets the title of the category, or the name if the title is not set.<p>
     *
     * @return the title or name
     */
    public Object getTitleOrName() {

        String result = getTitle();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = getPath();
        }
        return result;
    }

    /**
     * Sets the children.<p>
     *
     * @param children the children to set
     */
    public void setChildren(List<CmsCategoryTreeEntry> children) {

        m_children = children;
    }

    /**
     * Sets the 'forced visible' status.
     *
     * @param visibility the new value
     */
    public void setForcedVisible(Boolean visibility) {

        m_forcedVisible = visibility;
    }
}